package cofh.thermal.core.common.event;

import cofh.thermal.lib.util.ThermalRecipeManagers;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import static cofh.lib.util.constants.ModIds.ID_THERMAL;

@EventBusSubscriber (modid = ID_THERMAL)
public class TCoreCommonSetupEvents {

    private TCoreCommonSetupEvents() {

    }

    //    @SubscribeEvent
    //    public static void setupVillagerTrades(final VillagerTradesEvent event) {
    //
    //        if (!ThermalConfig.enableVillagerTrades.get()) {
    //            return;
    //        }
    //    }

    // region RELOAD
    @SubscribeEvent
    public static void addReloadListener(final AddReloadListenerEvent event) {

        event.addListener((ResourceManagerReloadListener) manager ->
                ThermalRecipeManagers.instance().setServerRecipeManager(event.getServerResources().getRecipeManager())
        );
    }

    // Recipes reload during TagsUpdatedEvent
    @SubscribeEvent
    public static void tagsUpdated(final TagsUpdatedEvent event) {

        var managers = ThermalRecipeManagers.instance();
        switch (event.getUpdateCause()) {
            case SERVER_DATA_LOAD -> managers.refreshServer(event.getRegistryAccess());
            case CLIENT_PACKET_RECEIVED -> managers.refreshClient(event.getRegistryAccess());
        }
    }

    // endregion
}
