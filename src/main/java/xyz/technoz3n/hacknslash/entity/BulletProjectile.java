package xyz.technoz3n.hacknslash.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import xyz.technoz3n.hacknslash.HackNSlash;

public class BulletProjectile extends ThrowableProjectile implements ItemSupplier {

    private static final float DAMAGE = 3.0F;

    public BulletProjectile(EntityType<? extends BulletProjectile> type, Level level) {
        super(type, level);
    }

    public BulletProjectile(Level level, LivingEntity shooter) {
        super(HackNSlash.BULLET_PROJECTILE.get(), shooter, level);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.YELLOW_CONCRETE);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide()) return;

        if (result.getEntity() instanceof LivingEntity living && getOwner() instanceof LivingEntity owner) {
            DamageSource source = level().damageSources().mobProjectile(this, owner);
            living.hurt(source, DAMAGE);
        }
        discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !level().isClientSide()) {
            discard();
        }
    }
}