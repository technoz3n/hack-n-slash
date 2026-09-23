package xyz.technoz3n.hacknslash;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = HackNSlash.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
                        .comment("meow :3")
                        .defineInRange("cat meter", 100, 0, Integer.MAX_VALUE);

        static final ForgeConfigSpec SPEC = BUILDER.build();

        public static int magicNumber;

        @SubscribeEvent
        static void onLoad(final ModConfigEvent event) {
                if (event.getConfig().getSpec() == SPEC) {
                        magicNumber = MAGIC_NUMBER.get();
                        LogUtils.getLogger().info("cat meter: " + magicNumber);
                }
        }
}
