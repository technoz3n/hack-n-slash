package xyz.technoz3n.hacknslash.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraftforge.network.NetworkHooks;
import xyz.technoz3n.hacknslash.HackNSlash;

public class RocketProjectile extends ThrowableProjectile {

    private static final double LAUNCH_RADIUS = 5.0;
    private static final double LAUNCH_STRENGTH = 1.8;

    public RocketProjectile(EntityType<? extends RocketProjectile> type, Level level) {
        super(type, level);
    }

    public RocketProjectile(Level level, LivingEntity shooter) {
        super(HackNSlash.ROCKET_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            explode(serverLevel);
        }
    }

    private void explode(ServerLevel level) {
        level.getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(LAUNCH_RADIUS)).forEach(entity -> {
                    Vec3 diff = entity.position().subtract(this.position());
                    double distance = diff.length();
                    if (distance > LAUNCH_RADIUS || distance < 0.01)
                        return;

                    double falloff = 1.0 - (distance / LAUNCH_RADIUS);
                    Vec3 direction = diff.normalize();

                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            direction.x * LAUNCH_STRENGTH * falloff,
                            Math.max(direction.y, 0.3) * LAUNCH_STRENGTH * falloff,
                            direction.z * LAUNCH_STRENGTH * falloff));
                    entity.hasImpulse = true;
                    entity.hurtMarked = true;
                });

        level.sendParticles(ParticleTypes.EXPLOSION, position().x, position().y, position().z,
                1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 1.0F, 1.0F);

        discard();
    }

    @Override
    protected float getGravity() {
        return 0.03F;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {

    }
}