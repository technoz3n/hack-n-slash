package xyz.technoz3n.hacknslash.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import xyz.technoz3n.hacknslash.event.AKFallDamageHandler;
import xyz.technoz3n.hacknslash.entity.BulletProjectile;
import java.util.Optional;

public class AKItem extends Item {

    private static final int MAX_AMMO = 30;
    private static final int AMMO_REGEN_TICKS = 10; // 0.5s per bullet regained
    private static final int FIRE_INTERVAL_TICKS = 4; // ~5 shots/sec full-auto
    private static final double RANGE = 40.0;
    private static final float DAMAGE_PER_SHOT = 3.0F;
    private static final double RECOIL_STRENGTH = 0.06; // subtle, per-shot

    private static final String TAG_AMMO = "hnsAkAmmo";
    private static final String TAG_NEXT_REGEN = "hnsAkNextRegen";

    public AKItem(Properties properties) {
        super(properties);
    }

    private int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag == null || !tag.contains(TAG_AMMO)) ? MAX_AMMO : tag.getInt(TAG_AMMO);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide())
            return;

        CompoundTag tag = stack.getOrCreateTag();
        int ammo = tag.contains(TAG_AMMO) ? tag.getInt(TAG_AMMO) : MAX_AMMO;
        if (ammo >= MAX_AMMO)
            return;

        long gameTime = level.getGameTime();
        long nextRegen = tag.contains(TAG_NEXT_REGEN) ? tag.getLong(TAG_NEXT_REGEN) : gameTime + AMMO_REGEN_TICKS;
        if (gameTime >= nextRegen) {
            tag.putInt(TAG_AMMO, Math.min(MAX_AMMO, ammo + 1));
            tag.putLong(TAG_NEXT_REGEN, gameTime + AMMO_REGEN_TICKS);
        } else if (!tag.contains(TAG_NEXT_REGEN)) {
            tag.putLong(TAG_NEXT_REGEN, nextRegen);
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
        if (level.isClientSide())
            return;
        if (!(entity instanceof Player player))
            return;

        int ticksUsed = getUseDuration(stack) - remainingUseTicks;
        if (ticksUsed % FIRE_INTERVAL_TICKS != 0)
            return;

        CompoundTag tag = stack.getOrCreateTag();
        int ammo = tag.contains(TAG_AMMO) ? tag.getInt(TAG_AMMO) : MAX_AMMO;
        if (ammo <= 0)
            return;

        tag.putInt(TAG_AMMO, ammo - 1);
        tag.putLong(TAG_NEXT_REGEN, level.getGameTime() + AMMO_REGEN_TICKS);

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