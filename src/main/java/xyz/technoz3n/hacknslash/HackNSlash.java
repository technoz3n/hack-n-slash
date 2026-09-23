package xyz.technoz3n.hacknslash;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(HackNSlash.MODID)
public class HackNSlash
{
    // the mod id duh
    public static final String MODID = "hacknslash";
    // directly reference a slf4j logger for whatever reason
    private static final Logger LOGGER = LogUtils.getLogger();
    // deferred register thing to hold items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    
    public HackNSlash(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        // register the deferred regisiter so mod items register to get registered (registerception >:3)
        ITEMS.register(modEventBus);
        // register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        
        // register the mod's ForgeConfigSpec so that Forge can create and load the config file for me
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // some (un)common setup code
        LOGGER.info("HI I LIKE CATS");
        LOGGER.info("cat percent: " + Config.magicNumber);
    }

    // i can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // do something when the server starts jeez
        LOGGER.info("HI I LIKE CATS but am from the server starting");
    }

    // i can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // some client setup code
            LOGGER.info("HELLO I AM A CLIENT CAT SETUP");
        }
    }
}
