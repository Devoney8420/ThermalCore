package cofh.thermal.lib.util.managers;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeManager;

public interface IManager {

    default void config() {

    }

    void refresh(RecipeManager recipeManager);

    default void refresh(RecipeManager recipeManager, HolderLookup.Provider registries) {

        refresh(recipeManager);
    }

}
