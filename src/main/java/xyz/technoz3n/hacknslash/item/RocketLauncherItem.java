package xyz.technoz3n.hacknslash.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.technoz3n.hacknslash.entity.RocketProjectile;

public class RocketLauncherItem extends Item {

    public RocketLauncherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            RocketProjectile rocket = new RocketProjectile(level, player);
            rocket.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, 1.5F, 0.0F);
            level.addFreshEntity(rocket);

            level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}