package cofh.thermal.core.client.event;

import cofh.core.client.event.CoreClientEvents;
import cofh.core.client.model.SimpleModel;
import cofh.core.client.renderer.entity.TNTMinecartRendererCoFH;
import cofh.lib.client.renderer.entity.TntRendererCoFH;
import cofh.thermal.core.client.gui.ChargeBenchScreen;
import cofh.thermal.core.client.gui.device.*;
import cofh.thermal.core.client.gui.storage.EnergyCellScreen;
import cofh.thermal.core.client.gui.storage.FluidCellScreen;
import cofh.thermal.core.client.gui.storage.SatchelScreen;
import cofh.thermal.core.client.gui.TinkerBenchScreen;
import cofh.thermal.core.client.renderer.entity.*;
import cofh.thermal.core.client.renderer.entity.layers.FestiveLayer;
import cofh.thermal.core.client.renderer.entity.model.*;
import cofh.thermal.core.client.renderer.model.*;
import cofh.thermal.core.common.config.ThermalClientConfig;
import cofh.thermal.core.common.entity.explosive.DetonateUtils;
import cofh.thermal.core.common.fluid.RedstoneFluid;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static cofh.lib.util.constants.ModIds.ID_THERMAL;
import static cofh.thermal.core.init.registries.TCoreEntities.*;
import static cofh.thermal.core.init.registries.TCoreMenus.*;

@EventBusSubscriber (value = Dist.CLIENT, modid = ID_THERMAL, bus = EventBusSubscriber.Bus.MOD)
public class TCoreClientSetupEvents {

    private TCoreClientSetupEvents() {

    }

    @SuppressWarnings ({"rawtypes", "unchecked"})
    @SubscribeEvent
    public static void addRenderLayers(final EntityRenderersEvent.AddLayers event) {

        if (!ThermalClientConfig.festiveVanillaMobs.get()) {
            return;
        }
        var blaze = event.getRenderer(EntityType.BLAZE);
        if (blaze instanceof LivingEntityRenderer livingEntityRenderer) {
            livingEntityRenderer.addLayer(new FestiveLayer<>(event.getContext(), (RenderLayerParent) blaze, -0.15F, 0.9F));
        }
        //        var creeper = event.getRenderer(EntityType.CREEPER);
        //        if (creeper instanceof LivingEntityRenderer<Creeper, ? extends EntityModel<Creeper>>) {
        //            creeper.addLayer(new FestiveLayer<>(event.getContext(), (RenderLayerParent) creeper, 0.0F, 0.9F));
        //        }
        var enderman = event.getRenderer(EntityType.ENDERMAN);
        if (enderman instanceof LivingEntityRenderer livingEntityRenderer) {
            livingEntityRenderer.addLayer(new FestiveLayer<>(event.getContext(), (RenderLayerParent) enderman, -1.15F, 0.9F));
        }
        //        var ghast = event.getRenderer(EntityType.GHAST);
        //        if (ghast instanceof LivingEntityRenderer<Ghast, ? extends EntityModel<Ghast>>) {
        //            ghast.addLayer(new FestiveLayer<>(event.getContext(), (RenderLayerParent) ghast, 0.75F, 1.5F));
        //        }
    }

    @SubscribeEvent
    public static void colorSetupBlock(final RegisterColorHandlersEvent.Block event) {

        // BlockColors colors = event.getBlockColors();

        // colors.register((state, reader, pos, tintIndex) -> (reader == null || pos == null) ? FoliageColors.getDefault() : BiomeColors.getFoliageColor(reader, pos), BLOCKS.get(ID_RUBBER_LEAVES));
    }

    @SubscribeEvent
    public static void registerModels(final RegisterGeometryLoaders event) {

        event.register(ResourceLocation.fromNamespaceAndPath(ID_THERMAL, "underlay"), new SimpleModel.Loader(UnderlayBakedModel::new));
        event.register(ResourceLocation.fromNamespaceAndPath(ID_THERMAL, "dynamo"), new SimpleModel.Loader(DynamoBakedModel::new));
        event.register(ResourceLocation.fromNamespaceAndPath(ID_THERMAL, "reconfigurable"), new SimpleModel.Loader(ReconfigurableBakedModel::new));
        event.register(ResourceLocation.fromNamespaceAndPath(ID_THERMAL, "energy_cell"), new SimpleModel.Loader(EnergyCellBakedModel::new));
        event.register(ResourceLocation.fromNamespaceAndPath(ID_THERMAL, "fluid_cell"), new SimpleModel.Loader(FluidCellBakedModel::new));
        event.register(ResourceLocation.fromNamespaceAndPath(ID_THERMAL, "item_cell"), new SimpleModel.Loader(ItemCellBakedModel::new));
    }

    @SubscribeEvent
    public static void entityLayerSetup(final EntityRenderersEvent.RegisterLayerDefinitions event) {

        event.registerLayerDefinition(BasalzModel.BASALZ_LAYER, BasalzModel::createBodyLayer);
        event.registerLayerDefinition(BlitzModel.BLITZ_LAYER, BlitzModel::createBodyLayer);
        event.registerLayerDefinition(BlizzModel.BLIZZ_LAYER, BlizzModel::createBodyLayer);

        event.registerLayerDefinition(SantaHatModel.HAT_LAYER, SantaHatModel::createBodyLayer);

        event.registerLayerDefinition(ElementalProjectileModel.PROJECTILE_LAYER, ElementalProjectileModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void entityRendererSetup(final EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(THROWN_FLORB.get(), ThrownItemRenderer::new);

        for (var grenade : DetonateUtils.GRENADES) {
            event.registerEntityRenderer(grenade.get(), ThrownItemRenderer::new);
        }
        for (var tnt : DetonateUtils.TNT) {
            event.registerEntityRenderer(tnt.get(), TntRendererCoFH::new);
        }
        for (var cart : DetonateUtils.CARTS) {
            event.registerEntityRenderer(cart.get(), TNTMinecartRendererCoFH::new);
        }
        event.registerEntityRenderer(BASALZ.get(), BasalzRenderer::new);
        event.registerEntityRenderer(BLITZ.get(), BlitzRenderer::new);
        event.registerEntityRenderer(BLIZZ.get(), BlizzRenderer::new);

        event.registerEntityRenderer(BASALZ_PROJECTILE.get(), BasalzProjectileRenderer::new);
        event.registerEntityRenderer(BLITZ_PROJECTILE.get(), BlitzProjectileRenderer::new);
        event.registerEntityRenderer(BLIZZ_PROJECTILE.get(), BlizzProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void menuScreenSetup(final RegisterMenuScreensEvent event) {

        event.register(DEVICE_HIVE_EXTRACTOR_CONTAINER.get(), DeviceHiveExtractorScreen::new);
        event.register(DEVICE_TREE_EXTRACTOR_CONTAINER.get(), DeviceTreeExtractorScreen::new);
        event.register(DEVICE_FISHER_CONTAINER.get(), DeviceFisherScreen::new);
        event.register(DEVICE_COMPOSTER_CONTAINER.get(), DeviceComposterScreen::new);
        event.register(DEVICE_SOIL_INFUSER_CONTAINER.get(), DeviceSoilInfuserScreen::new);
        event.register(DEVICE_WATER_GEN_CONTAINER.get(), DeviceWaterGenScreen::new);
        event.register(DEVICE_ROCK_GEN_CONTAINER.get(), DeviceRockGenScreen::new);
        event.register(DEVICE_COLLECTOR_CONTAINER.get(), DeviceCollectorScreen::new);
        event.register(DEVICE_XP_CONDENSER_CONTAINER.get(), DeviceXpCondenserScreen::new);
        event.register(DEVICE_POTION_DIFFUSER_CONTAINER.get(), DevicePotionDiffuserScreen::new);
        event.register(DEVICE_NULLIFIER_CONTAINER.get(), DeviceNullifierScreen::new);
        event.register(TINKER_BENCH_CONTAINER.get(), TinkerBenchScreen::new);
        event.register(CHARGE_BENCH_CONTAINER.get(), ChargeBenchScreen::new);
        event.register(SATCHEL_CONTAINER.get(), SatchelScreen::new);
        event.register(ENERGY_CELL_CONTAINER.get(), EnergyCellScreen::new);
        event.register(FLUID_CELL_CONTAINER.get(), FluidCellScreen::new);

        // event.register(ITEM_CELL_CONTAINER, ItemCellScreen::new);
    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {

        event.enqueueWork(TCoreClientSetupEvents::registerRenderLayers);

        event.enqueueWork(() -> CoreClientEvents.addNamespace(ID_THERMAL));
    }

    private static void registerRenderLayers() {

        ItemBlockRenderTypes.setRenderLayer(RedstoneFluid.instance().still().get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(RedstoneFluid.instance().flowing().get(), RenderType.translucent());
    }

}
