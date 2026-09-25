package xyz.technoz3n.hacknslash;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
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
import net.minecraftforge.registries.RegistryObject;
import xyz.technoz3n.hacknslash.enchantment.SoulPullEnchantment;
import xyz.technoz3n.hacknslash.entity.RocketProjectile;
import xyz.technoz3n.hacknslash.item.RocketLauncherItem;
import xyz.technoz3n.hacknslash.item.ScytheItem;

@Mod(HackNSlash.MODID)
public class HackNSlash {
        // the mod id duh
        public static final String MODID = "hacknslash";
        // directly reference a slf4j logger for whatever reason
        private static final Logger LOGGER = LogUtils.getLogger();
        // deferred register thing to hold items
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
        // make the scythe
        public static final RegistryObject<Item> SCYTHE = ITEMS.register("scythe",
                        () -> new ScytheItem(Tiers.NETHERITE, 8, -2.8F, new Item.Properties()));
        // rocket launcher
        public static final RegistryObject<Item> ROCKET_LAUNCHER = ITEMS.register("rocket_launcher",
                        () -> new RocketLauncherItem(new Item.Properties().stacksTo(1).durability(64)));
        // same thing, but for enchantments
        public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister
                        .create(ForgeRegistries.ENCHANTMENTS, MODID);
        // make the enchantment
        public static final RegistryObject<Enchantment> SOUL_PULL = ENCHANTMENTS.register("soul_pull",
                        () -> new SoulPullEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
        // now we do sounds
        public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
                        .create(ForgeRegistries.SOUND_EVENTS, MODID);

        public static final RegistryObject<SoundEvent> SOUL_PULL_SOUND = SOUND_EVENTS.register("scythe.soul_pull",
                        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "scythe.soul_pull")));
        public static final RegistryObject<SoundEvent> SCYTHE_WHOOSH = SOUND_EVENTS.register("scythe.whoosh",
                        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "scythe.whoosh")));
        public static final RegistryObject<SoundEvent> SCYTHE_SWING_HIT = SOUND_EVENTS.register("scythe.swinghit",
                        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "scythe.swinghit")));
        public static final RegistryObject<SoundEvent> SCYTHE_SLASH = SOUND_EVENTS.register("scythe.slash",
                        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "scythe.slash")));
        public static final RegistryObject<SoundEvent> SCYTHE_WINDUP = SOUND_EVENTS.register("scythe.windup",
                        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "scythe.windup")));
        // register entities
        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
                        .create(ForgeRegistries.ENTITY_TYPES, MODID);
        public static final RegistryObject<EntityType<RocketProjectile>> ROCKET_PROJECTILE = ENTITY_TYPES.register(
                        "rocket_projectile",
                        () -> EntityType.Builder.<RocketProjectile>of(RocketProjectile::new, MobCategory.MISC)
                                        .sized(0.25F, 0.25F)
                                        .clientTrackingRange(64)
                                        .updateInterval(10)
                                        .build("rocket_projectile"));

        public HackNSlash(FMLJavaModLoadingContext context) {
                IEventBus modEventBus = context.getModEventBus();

                modEventBus.addListener(this::commonSetup);

                // register the deferred regisiter so mod items register to get registered
                // (registerception >:3)
                ITEMS.register(modEventBus);
                ENCHANTMENTS.register(modEventBus);
                SOUND_EVENTS.register(modEventBus);
                ENTITY_TYPES.register(modEventBus);
                // register ourselves for server and other game events we are interested in
                MinecraftForge.EVENT_BUS.register(this);

                // register the mod's ForgeConfigSpec so that Forge can create and load the
                // config file for me
                context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        }

        private void commonSetup(final FMLCommonSetupEvent event) {
                // some (un)common setup code
                LOGGER.info("HI I LIKE CATS");
                LOGGER.info("cat percent: " + Config.magicNumber);
        }

        // i can use SubscribeEvent and let the Event Bus discover methods to call
        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event) {
                // do something when the server starts jeez
                LOGGER.info("HI I LIKE CATS but am from the server starting");
        }

        // i can use EventBusSubscriber to automatically register all static methods in
        // the class annotated with @SubscribeEvent
        @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
        public static class ClientModEvents {
                @SubscribeEvent
                public static void onClientSetup(FMLClientSetupEvent event) {
                        // some client setup code
                        LOGGER.info("HELLO I AM A CLIENT CAT SETUP");
                }

                @SubscribeEvent
                public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
                        event.registerEntityRenderer(ROCKET_PROJECTILE.get(), NoopRenderer::new);
                }

                @SubscribeEvent
                public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
                        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                                event.accept(SCYTHE);
                        }
                }
        }
}
