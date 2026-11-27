package cofh.thermal.core;

import cofh.core.common.config.ConfigManager;
import cofh.core.common.config.world.FeatureConfig;
import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.api.item.IFluidContainerItem;
import cofh.lib.common.energy.EnergyContainerItemWrapper;
import cofh.lib.common.fluid.FluidContainerItemWrapper;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.thermal.core.common.config.*;
import cofh.thermal.core.common.entity.monster.Basalz;
import cofh.thermal.core.common.entity.monster.Blitz;
import cofh.thermal.core.common.entity.monster.Blizz;
import cofh.thermal.core.init.registries.*;
import cofh.thermal.lib.util.ThermalProxy;
import cofh.thermal.lib.util.ThermalProxyClient;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cofh.lib.util.FlagManager.getFlag;
import static cofh.lib.util.FlagManager.setFlag;
import static cofh.lib.util.constants.ModIds.ID_THERMAL;
import static cofh.thermal.core.init.registries.TCoreEntities.*;
import static cofh.thermal.lib.util.ThermalFlags.*;
import static cofh.thermal.lib.util.ThermalIDs.ID_TINKER_BENCH;
import static cofh.thermal.lib.util.ThermalIDs.ID_ENERGY_CELL;
import static cofh.thermal.lib.util.ThermalIDs.ID_FLUID_CELL;

@Mod (ID_THERMAL)
public class ThermalCore {

    public static final Logger LOG = LogManager.getLogger(ID_THERMAL);
    public static final ThermalProxy PROXY = FMLEnvironment.dist.isClient() ? new ThermalProxyClient() : new ThermalProxy();
    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();

    public static final DeferredRegisterCoFH<Block> BLOCKS = DeferredRegisterCoFH.create(BuiltInRegistries.BLOCK, ID_THERMAL);
    public static final DeferredRegisterCoFH<Item> ITEMS = DeferredRegisterCoFH.create(BuiltInRegistries.ITEM, ID_THERMAL);
    public static final DeferredRegisterCoFH<Fluid> FLUIDS = DeferredRegisterCoFH.create(BuiltInRegistries.FLUID, ID_THERMAL);
    public static final DeferredRegisterCoFH<CreativeModeTab> CREATIVE_TABS = DeferredRegisterCoFH.create(BuiltInRegistries.CREATIVE_MODE_TAB, ID_THERMAL);
    public static final DeferredRegisterCoFH<MobEffect> EFFECTS = DeferredRegisterCoFH.create(BuiltInRegistries.MOB_EFFECT, ID_THERMAL);

    public static final DeferredRegisterCoFH<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegisterCoFH.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID_THERMAL);
    public static final DeferredRegisterCoFH<MenuType<?>> CONTAINERS = DeferredRegisterCoFH.create(BuiltInRegistries.MENU, ID_THERMAL);
    public static final DeferredRegisterCoFH<EntityType<?>> ENTITIES = DeferredRegisterCoFH.create(BuiltInRegistries.ENTITY_TYPE, ID_THERMAL);
    public static final DeferredRegisterCoFH<RecipeType<?>> RECIPE_TYPES = DeferredRegisterCoFH.create(BuiltInRegistries.RECIPE_TYPE, ID_THERMAL);
    public static final DeferredRegisterCoFH<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegisterCoFH.create(BuiltInRegistries.RECIPE_SERIALIZER, ID_THERMAL);
    public static final DeferredRegisterCoFH<SoundEvent> SOUND_EVENTS = DeferredRegisterCoFH.create(BuiltInRegistries.SOUND_EVENT, ID_THERMAL);

    public static final DeferredRegisterCoFH<Feature<?>> FEATURES = DeferredRegisterCoFH.create(Registries.FEATURE, ID_THERMAL);
    public static final DeferredRegisterCoFH<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegisterCoFH.create(Registries.PLACEMENT_MODIFIER_TYPE, ID_THERMAL);

    public static final DeferredRegisterCoFH<FluidType> FLUID_TYPES = DeferredRegisterCoFH.create(NeoForgeRegistries.Keys.FLUID_TYPES, ID_THERMAL);
    public static final DeferredRegisterCoFH<MapCodec<? extends IGlobalLootModifier>> LOOT_SERIALIZERS = DeferredRegisterCoFH.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ID_THERMAL);

    public ThermalCore(ModContainer modContainer, IEventBus modEventBus) {

        setFeatureFlags();
        addOreConfigs();

        CONFIG_MANAGER.register(modContainer, modEventBus)
                .addClientConfig(new ThermalClientConfig())
                .addServerConfig(new ThermalCoreConfig())
                .addServerConfig(new ThermalDeviceConfig())
                .addServerConfig(new ThermalRecipeConfig())
                .addCommonConfig(new ThermalWorldConfig());

        modEventBus.addListener(this::entityAttributeSetup);
        modEventBus.addListener(this::spawnPlacementSetup);
        modEventBus.addListener(this::capabilitySetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registrySetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        EFFECTS.register(modEventBus);

        BLOCK_ENTITIES.register(modEventBus);
        CONTAINERS.register(modEventBus);
        ENTITIES.register(modEventBus);
        LOOT_SERIALIZERS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        PLACEMENT_MODIFIERS.register(modEventBus);

        FLUID_TYPES.register(modEventBus);

        TCoreBlocks.register();
        TCoreItems.register();
        TCoreFluids.register();
        ThermalCreativeTabs.register();

        TCoreBlockEntities.register();
        TCoreMenus.register();
        TCoreEntities.register();
        TCoreRecipeSerializers.register();
        TCoreRecipeTypes.register();
        TCoreSounds.register();

        TCoreRecipeManagers.register();
        TCorePlacementModifiers.register();
    }

    private void setFeatureFlags() {

        setFlag(FLAG_FILTER_AUGMENTS, true);
        setFlag(FLAG_STORAGE_AUGMENTS, true);
        setFlag(FLAG_UPGRADE_AUGMENTS, true);

        setFlag(FLAG_CREATIVE_AUGMENTS, true);

        setFlag(ID_TINKER_BENCH, true);

        // setFlag(ID_CHUNK_LOADER, true);
    }

    private void addOreConfigs() {

        ThermalWorldConfig.addFeatureConfig("niter_ore", new FeatureConfig("Niter", getFlag(FLAG_RESOURCE_NITER)));
        ThermalWorldConfig.addFeatureConfig("sulfur_ore", new FeatureConfig("Sulfur", getFlag(FLAG_RESOURCE_SULFUR)));

        ThermalWorldConfig.addFeatureConfig("tin_ore", new FeatureConfig("Tin", getFlag(FLAG_RESOURCE_TIN)));
        ThermalWorldConfig.addFeatureConfig("lead_ore", new FeatureConfig("Lead", getFlag(FLAG_RESOURCE_LEAD)));
        ThermalWorldConfig.addFeatureConfig("silver_ore", new FeatureConfig("Silver", getFlag(FLAG_RESOURCE_SILVER)));
        ThermalWorldConfig.addFeatureConfig("nickel_ore", new FeatureConfig("Nickel", getFlag(FLAG_RESOURCE_NICKEL)));

        ThermalWorldConfig.addFeatureConfig("apatite_ore", new FeatureConfig("Apatite", getFlag(FLAG_RESOURCE_APATITE)));

        ThermalWorldConfig.addFeatureConfig("cinnabar_ore", new FeatureConfig("Cinnabar", getFlag(FLAG_RESOURCE_CINNABAR)));
        ThermalWorldConfig.addFeatureConfig("oil_sand", new FeatureConfig("Oil Sand", getFlag(FLAG_RESOURCE_OIL)));

        ThermalWorldConfig.addFeatureConfig("rubberwood_trees", new FeatureConfig("Rubberwood Trees", getFlag(FLAG_RESOURCE_RUBBERWOOD)));
    }

    // region INITIALIZATION
    private void entityAttributeSetup(final EntityAttributeCreationEvent event) {

        event.put(BASALZ.get(), Basalz.registerAttributes().build());
        event.put(BLITZ.get(), Blitz.registerAttributes().build());
        event.put(BLIZZ.get(), Blizz.registerAttributes().build());
    }

    private void spawnPlacementSetup(final RegisterSpawnPlacementsEvent event) {

        event.register(BASALZ.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Basalz::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BLITZ.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Blitz::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BLIZZ.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Blizz::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private void capabilitySetup(RegisterCapabilitiesEvent event) {

        TCoreBlockEntities.capabilitySetup(event);
        Item energyCell = ITEMS.get(ID_ENERGY_CELL);
        IEnergyContainerItem energyContainer = (IEnergyContainerItem) energyCell;
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new EnergyContainerItemWrapper(stack, energyContainer), energyCell);
        Item fluidCell = ITEMS.get(ID_FLUID_CELL);
        IFluidContainerItem fluidContainer = (IFluidContainerItem) fluidCell;
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FluidContainerItemWrapper(stack, fluidContainer), fluidCell);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(TCoreBlocks::setup);
        event.enqueueWork(TCoreItems::setup);
        event.enqueueWork(TCoreFluids::setup);
        event.enqueueWork(TCoreEntities::setup);
    }

    private void registrySetup(final NewRegistryEvent event) {

        CONFIG_MANAGER.setupClient();
        CONFIG_MANAGER.setupServer();
        CONFIG_MANAGER.setupCommon();
    }
    // endregion
}
