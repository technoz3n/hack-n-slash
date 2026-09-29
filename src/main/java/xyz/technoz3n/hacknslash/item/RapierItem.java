package xyz.technoz3n.hacknslash.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.technoz3n.hacknslash.event.RapierDashHandler;

public class RapierItem extends SwordItem {

    private static final int MAX_CHARGES = 3;
    private static final int RECHARGE_TICKS = 30; // 1.5s per charge
    private static final int DEPLETED_LOCKOUT_TICKS = 20; // 1s extra lockout after hitting 0
    private static final int THROW_THRESHOLD_TICKS = 10; // min hold before release counts as a real dash
    private static final int USE_DURATION = 72000; // hold-until-released
    private static final double DASH_STRENGTH = 3;
    private static final int DASH_ATTACK_TICKS = 8; // ram-damage window duration
    private static final double MIN_DASH_STRENGTH = 0.8; // bare minimum, just above the throw threshold
    private static final int MAX_CHARGE_TICKS = 30; // 1.5s to reach full charge/speed

    private static final String TAG_CHARGES = "hnsRapierCharges";
    private static final String TAG_NEXT_REGEN = "hnsRapierNextRegen";
    private static final String TAG_LOCKOUT_UNTIL = "hnsRapierLockoutUntil";

    public RapierItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    private int getCharges(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CHARGES))
            return MAX_CHARGES;
        return tag.getInt(TAG_CHARGES);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide())
            return;

        CompoundTag tag = stack.getOrCreateTag();
        int charges = tag.contains(TAG_CHARGES) ? tag.getInt(TAG_CHARGES) : MAX_CHARGES;

        long gameTime = level.getGameTime();
        long lockoutUntil = tag.getLong(TAG_LOCKOUT_UNTIL);

        if (charges < MAX_CHARGES && gameTime >= lockoutUntil) {
            long nextRegen = tag.contains(TAG_NEXT_REGEN) ? tag.getLong(TAG_NEXT_REGEN) : gameTime + RECHARGE_TICKS;
            if (gameTime >= nextRegen) {
                tag.putInt(TAG_CHARGES, Math.min(MAX_CHARGES, charges + 1));
                tag.putLong(TAG_NEXT_REGEN, gameTime + RECHARGE_TICKS);
            } else if (!tag.contains(TAG_NEXT_REGEN)) {
                tag.putLong(TAG_NEXT_REGEN, nextRegen);
            }
        }

        if (selected && entity instanceof Player player) {
            int displayCharges = tag.contains(TAG_CHARGES) ? tag.getInt(TAG_CHARGES) : MAX_CHARGES;
            String pips = "●".repeat(displayCharges) + "○".repeat(MAX_CHARGES - displayCharges);

            MutableComponent message;
            if (gameTime < lockoutUntil) {
                float secondsLeft = (lockoutUntil - gameTime) / 20.0F;
                message = Component.literal(pips + String.format(" (recharging in %.1fs)", secondsLeft));
            } else {
                message = Component.literal(pips);
            }
            player.displayClientMessage(message, true);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getCharges(stack) <= 0) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player))
            return;

        int ticksCharged = getUseDuration(stack) - timeLeft;
        if (ticksCharged < THROW_THRESHOLD_TICKS)
            return;

        CompoundTag tag = stack.getOrCreateTag();
        int charges = tag.contains(TAG_CHARGES) ? tag.getInt(TAG_CHARGES) : MAX_CHARGES;
        if (charges <= 0)
            return;

        charges -= 1;
        tag.putInt(TAG_CHARGES, charges);
        if (charges <= 0) {
            tag.putLong(TAG_LOCKOUT_UNTIL, level.getGameTime() + DEPLETED_LOCKOUT_TICKS);
        }
        tag.putLong(TAG_NEXT_REGEN, level.getGameTime() + RECHARGE_TICKS);

        int cappedTicks = Math.min(ticksCharged, MAX_CHARGE_TICKS);
        double chargeRatio = cappedTicks / (double) MAX_CHARGE_TICKS;
        double actualStrength = MIN_DASH_STRENGTH + (DASH_STRENGTH - MIN_DASH_STRENGTH) * chargeRatio;

        Vec3 look = player.getLookAngle();
        Vec3 dashVelocity = look.normalize().scale(actualStrength);
        player.setDeltaMovement(player.getDeltaMovement().add(dashVelocity));
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.hurtMarked = true;

        float ramDamage = (float) (dashVelocity.length() * 3.0);
        player.startAutoSpinAttack(DASH_ATTACK_TICKS);

        if (level instanceof ServerLevel serverLevel) {
            RapierDashHandler.beginDash(player, ramDamage, DASH_ATTACK_TICKS);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel2) {
            serverLevel2.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(),
                    12, 0.3, 0.3, 0.3, 0.1);
        }
    }
}