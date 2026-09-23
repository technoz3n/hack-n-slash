package xyz.technoz3n.hacknslash.enchantment;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;
import xyz.technoz3n.hacknslash.HackNSlash;
import xyz.technoz3n.hacknslash.item.ScytheItem;

public class SoulPullEnchantment extends Enchantment {

    private static final double PULL_STRENGTH = 0.35;

    public SoulPullEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof ScytheItem;
    }

    public static void performPull(ServerLevel level, LivingEntity attacker, LivingEntity target, int enchantLevel) {
        Vec3 diff = attacker.position().subtract(target.position());
        Vec3 direction = new Vec3(diff.x, 0, diff.z);
        if (direction.lengthSqr() > 1.0E-4) {
            direction = direction.normalize();
        }

        double strength = PULL_STRENGTH + (0.15 * (enchantLevel - 1));

        target.setDeltaMovement(direction.x * strength, 0.05, direction.z * strength);
        target.hasImpulse = true;
        target.hurtMarked = true;

        level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 0.5, target.getZ(),
                8, 0.2, 0.2, 0.2, 0.02);
        level.playSound(null, target.blockPosition(), HackNSlash.SOUL_PULL_SOUND.get(), SoundSource.PLAYERS, 0.6F, 0.8F);
    }
}