package xyz.technoz3n.hacknslash.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.technoz3n.hacknslash.HackNSlash;

import java.util.*;

@Mod.EventBusSubscriber(modid = HackNSlash.MODID)
public class RapierDashHandler {

    private static final int MAX_PIERCE_TARGETS = 5;

    private record ActiveDash(float damage, int ticksLeft, Set<UUID> alreadyHit) {}

    private static final Map<UUID, ActiveDash> activeDashes = new HashMap<>();

    public static void beginDash(Player player, float damage, int durationTicks) {
        activeDashes.put(player.getUUID(), new ActiveDash(damage, durationTicks, new HashSet<>()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player.level() instanceof ServerLevel serverLevel)) return;

        Player player = event.player;
        ActiveDash dash = activeDashes.get(player.getUUID());
        if (dash == null) return;

        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(0.5), e -> e != player && e.isAlive());

        for (LivingEntity target : nearby) {
            if (dash.alreadyHit().size() >= MAX_PIERCE_TARGETS) break;
            if (dash.alreadyHit().add(target.getUUID())) {
                target.hurt(player.damageSources().playerAttack(player), dash.damage());
            }
        }

        boolean pierceCapReached = dash.alreadyHit().size() >= MAX_PIERCE_TARGETS;
        int ticksLeft = dash.ticksLeft() - 1;

        if (pierceCapReached || ticksLeft <= 0) {
            activeDashes.remove(player.getUUID());
        } else {
            activeDashes.put(player.getUUID(), new ActiveDash(dash.damage(), ticksLeft, dash.alreadyHit()));
        }
    }
}