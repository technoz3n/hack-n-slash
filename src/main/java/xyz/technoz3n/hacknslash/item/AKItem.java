package xyz.technoz3n.hacknslash.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.technoz3n.hacknslash.entity.BulletProjectile;
import xyz.technoz3n.hacknslash.event.AKFallDamageHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AKItem extends Item {

    private static final int MAX_AMMO = 30;
    private static final int AMMO_REGEN_TICKS = 10;
    private static final int FIRE_INTERVAL_TICKS = 4;
    private static final float DAMAGE_PER_SHOT = 3.0F;
    private static final double RECOIL_STRENGTH = 0.06;

    private static final Map<UUID, Integer> ammoByPlayer = new HashMap<>();
    private static final Map<UUID, Long> nextRegenByPlayer = new HashMap<>();

    public AKItem(Properties properties) {
        super(properties);
    }

    private int getAmmo(Player player) {
        return ammoByPlayer.getOrDefault(player.getUUID(), MAX_AMMO);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        UUID id = player.getUUID();
        int ammo = ammoByPlayer.getOrDefault(id, MAX_AMMO);

        if (ammo < MAX_AMMO) {
            long gameTime = level.getGameTime();
            long nextRegen = nextRegenByPlayer.getOrDefault(id, gameTime + AMMO_REGEN_TICKS);
            if (gameTime >= nextRegen) {
                ammoByPlayer.put(id, Math.min(MAX_AMMO, ammo + 1));
                nextRegenByPlayer.put(id, gameTime + AMMO_REGEN_TICKS);
            } else {
                nextRegenByPlayer.putIfAbsent(id, nextRegen);
            }
        }

        if (selected) {
            int displayAmmo = ammoByPlayer.getOrDefault(id, MAX_AMMO);
            player.displayClientMessage(Component.literal("Ammo: " + displayAmmo + " / " + MAX_AMMO), true);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        int ticksUsed = getUseDuration(stack) - remainingUseTicks;
        if (ticksUsed % FIRE_INTERVAL_TICKS != 0) return;

        UUID id = player.getUUID();
        int ammo = ammoByPlayer.getOrDefault(id, MAX_AMMO);
        if (ammo <= 0) return;

        ammoByPlayer.put(id, ammo - 1);
        nextRegenByPlayer.put(id, level.getGameTime() + AMMO_REGEN_TICKS);

        fireShot(level, player);
    }

    private void fireShot(Level level, Player player) {
        BulletProjectile bullet = new BulletProjectile(level, player);
        bullet.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, 3.5F, 0.5F);
        level.addFreshEntity(bullet);

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(-RECOIL_STRENGTH)));
        player.hasImpulse = true;

        if (!player.onGround()) {
            AKFallDamageHandler.markMidairShot(player);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.0F, 2.0F);
    }
}