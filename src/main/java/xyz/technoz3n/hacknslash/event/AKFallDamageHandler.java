package xyz.technoz3n.hacknslash.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.technoz3n.hacknslash.HackNSlash;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = HackNSlash.MODID)
public class AKFallDamageHandler {

    private static final int WINDOW_TICKS = 60; // midair shot stays "recent" for 3 seconds
    private static final float FALL_DAMAGE_MULTIPLIER = 0.4F; // negates 60% of fall damage

    private static final Map<UUID, Long> lastMidairShot = new HashMap<>();

    public static void markMidairShot(Player player) {
        lastMidairShot.put(player.getUUID(), player.level().getGameTime());
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Long lastShot = lastMidairShot.get(player.getUUID());
        if (lastShot == null) return;

        long now = player.level().getGameTime();
        if (now - lastShot <= WINDOW_TICKS) {
            event.setDamageMultiplier(FALL_DAMAGE_MULTIPLIER);
            lastMidairShot.remove(player.getUUID());
        }
    }
}