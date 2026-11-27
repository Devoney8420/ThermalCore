package cofh.thermal.core.util.managers.dynamo;

import cofh.thermal.core.ThermalCore;
import cofh.thermal.core.util.recipes.dynamo.DisenchantmentFuel;
import cofh.thermal.lib.util.managers.SingleItemFuelManager;
import cofh.thermal.lib.util.recipes.internal.IDynamoFuel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.ArrayList;
import java.util.List;

import static cofh.lib.util.Utils.getRegistryName;
import static cofh.lib.util.constants.ModIds.ID_THERMAL;
import static cofh.thermal.core.init.registries.TCoreRecipeTypes.DISENCHANTMENT_FUEL;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class DisenchantmentFuelManager extends SingleItemFuelManager {

    private static final DisenchantmentFuelManager INSTANCE = new DisenchantmentFuelManager();
    protected static final int DEFAULT_ENERGY = 16000;

    public static DisenchantmentFuelManager instance() {

        return INSTANCE;
    }

    private DisenchantmentFuelManager() {

        super(DEFAULT_ENERGY);
    }

    @Override
    public boolean validFuel(ItemStack input) {

        if (input.getCapability(Capabilities.FluidHandler.ITEM) != null) {
            return false;
        }
        return getEnergy(input) > 0;
    }

    @Override
    protected void clear() {

        fuelMap.clear();
    }

    public int getEnergy(ItemStack stack) {

        IDynamoFuel fuel = getFuel(stack);
        return fuel != null ? fuel.getEnergy() : getEnergyFromEnchantments(stack);
    }

    public int getEnergyFromEnchantments(ItemStack stack) {

        if (stack.isEmpty()) {
            return 0;
        }
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        long energy = 0;
        int count = 0;
        for (var entry : enchantments.entrySet()) {
            energy += entry.getKey().value().getMinCost(entry.getIntValue());
            energy += ++count;
        }
        return Math.clamp(energy * (DEFAULT_ENERGY / 2), 0, Integer.MAX_VALUE);
    }

    // region IManager
    @Override
    public void refresh(RecipeManager recipeManager) {

        clear();
        convertedFuels = List.of();
        for (var entry : recipeManager.getAllRecipesFor(DISENCHANTMENT_FUEL.get())) {
            addFuel(entry.value());
        }
    }

    @Override
    public void refresh(RecipeManager recipeManager, HolderLookup.Provider registries) {

        refresh(recipeManager);
        registries.lookup(Registries.ENCHANTMENT).ifPresent(this::createConvertedRecipes);
    }
    // endregion

    // region CONVERSION
    protected volatile List<RecipeHolder<DisenchantmentFuel>> convertedFuels = List.of();

    public List<RecipeHolder<DisenchantmentFuel>> getConvertedFuels() {

        return convertedFuels;
    }

    protected void createConvertedRecipes(HolderLookup.RegistryLookup<Enchantment> registry) {

        List<RecipeHolder<DisenchantmentFuel>> generated = new ArrayList<>();
        registry.listElements().forEach(enchantment -> {
            var book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.value().getMaxLevel()));
            try {
                if (getFuel(book) == null && validFuel(book)) {
                    generated.add(convert(book, enchantment, getEnergy(book)));
                }
            } catch (Exception e) {
                ThermalCore.LOG.error("Could not preview disenchantment fuel for {} on {}", enchantment.key().location(), getRegistryName(book.getItem()), e);
            }
        });
        convertedFuels = List.copyOf(generated);
    }

    protected RecipeHolder<DisenchantmentFuel> convert(ItemStack item, Holder.Reference<Enchantment> enchantment, int energy) {

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ID_THERMAL,
                "disenchantment/" + enchantment.key().location().getNamespace() + "/" + enchantment.key().location().getPath());
        return new RecipeHolder<>(id, new DisenchantmentFuel(energy, singletonList(DataComponentIngredient.of(true, item)), emptyList()));
    }
    // endregion
}
