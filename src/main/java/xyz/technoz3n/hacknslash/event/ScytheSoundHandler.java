package xyz.technoz3n.hacknslash.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.technoz3n.hacknslash.HackNSlash;
import xyz.technoz3n.hacknslash.enchantment.SoulPullEnchantment;
import xyz.technoz3n.hacknslash.item.ScytheItem;

@Mod.EventBusSubscriber(modid = HackNSlash.MODID)

public class ScytheSoundHandler {
    private static final java.util.Set<java.util.UUID> pendingSoulPullTargets = new java.util.HashSet<>();

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        System.out.println("Held item: " + player.getMainHandItem().getItem().getClass());
        if (player.getMainHandItem().getItem() instanceof ScytheItem) {
            System.out.println("Scythe check passed, playing whoosh");
            player.level().playSound(player, player.blockPosition(),
                    HackNSlash.SCYTHE_WHOOSH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker))
            return;

        ItemStack heldStack = attacker.getMainHandItem();
        if (!(heldStack.getItem() instanceof ScytheItem))
            return;

        int pullLevel = heldStack.getEnchantmentLevel(HackNSlash.SOUL_PULL.get());
        if (pullLevel > 0 && attacker.level() instanceof ServerLevel serverLevel) {
            pendingSoulPullTargets.add(event.getEntity().getUUID());
            SoulPullEnchantment.performPull(serverLevel, attacker, event.getEntity(), pullLevel);
        }
    }

    @SubscribeEvent
    public static void onLivingKnockback(LivingKnockBackEvent event) {
        if (pendingSoulPullTargets.remove(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.getMainHandItem().getItem() instanceof ScytheItem && event.isVanillaCritical()) {
            player.level().playSound(null, player.blockPosition(),
                    HackNSlash.SCYTHE_SWING_HIT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}