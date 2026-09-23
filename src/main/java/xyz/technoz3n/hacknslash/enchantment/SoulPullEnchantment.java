package xyz.technoz3n.hacknslash.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.technoz3n.hacknslash.HackNSlash;
import xyz.technoz3n.hacknslash.item.ScytheItem;

import java.util.List;

public class SoulPullEnchantment extends Enchantment {

    private static final double BASE_RADIUS = 4.0;
    private static final double RADIUS_PER_LEVEL = 2.0;
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

    public static void performPull(ServerLevel level, LivingEntity attacker, BlockPos center, int enchantLevel) {
        double radius = BASE_RADIUS + (RADIUS_PER_LEVEL * (enchantLevel - 1));

        AABB area = new AABB(center).inflate(radius);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != attacker && e.isAlive());

        for (LivingEntity entity : nearby) {
            Vec3 diff = attacker.position().subtract(entity.position());
            Vec3 direction = new Vec3(diff.x, 0, diff.z);
            if (direction.lengthSqr() > 1.0E-4) {
                direction = direction.normalize();
            }

            double strength = PULL_STRENGTH + (0.1 * (enchantLevel - 1)); // scales a bit with level now too
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    direction.x * strength, 0.15, direction.z * strength));
            entity.hasImpulse = true;
            entity.hurtMarked = true;

            level.sendParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + 0.5, entity.getZ(),
                    8, 0.2, 0.2, 0.2, 0.02);
        }

        level.playSound(null, center, HackNSlash.SOUL_PULL_SOUND.get(), SoundSource.PLAYERS, 0.6F, 0.8F);
    }
}