package xyz.technoz3n.hacknslash.event;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.technoz3n.hacknslash.HackNSlash;
import xyz.technoz3n.hacknslash.item.ScytheItem;

@Mod.EventBusSubscriber(modid = HackNSlash.MODID)
public class ScytheSoundHandler {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        System.out.println("Held item: " + player.getMainHandItem().getItem().getClass());
        if (player.getMainHandItem().getItem() instanceof ScytheItem) {
            System.out.println("Scythe check passed, playing whoosh");
            player.level().playSound(null, player.blockPosition(),
                    HackNSlash.SCYTHE_WHOOSH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
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