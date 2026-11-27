package cofh.thermal.lib.util.recipes;

import cofh.lib.common.fluid.FluidIngredient;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.recipes.JsonMapCodec;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static cofh.lib.util.recipes.RecipeJsonUtils.*;

public class MachineRecipeSerializer<T extends ThermalRecipe> implements RecipeSerializer<T> {

    protected final int defaultEnergy;
    protected final IFactory<T> factory;

    public MachineRecipeSerializer(IFactory<T> factory, int defaultEnergy) {

        this.factory = factory;
        this.defaultEnergy = defaultEnergy;
    }

    @Override
    public MapCodec<T> codec() {

        return JsonMapCodec.INSTANCE
                .flatXmap(json -> {
                    try {
                        return DataResult.success(fromJson(json));
                    } catch (JsonParseException | IllegalStateException e) {
                        return DataResult.error(e::getMessage);
                    }
                }, recipe -> {
                    try {
                        return DataResult.success(toJson(recipe));
                    } catch (IllegalStateException e) {
                        return DataResult.error(e::getMessage);
                    }
                });
    }

    protected T fromJson(JsonObject json) {

        int energy = defaultEnergy;
        float experience = 0.0F;

        ArrayList<Ingredient> inputItems = new ArrayList<>();
        ArrayList<FluidIngredient> inputFluids = new ArrayList<>();
        ArrayList<ItemStack> outputItems = new ArrayList<>();
        ArrayList<Float> outputItemChances = new ArrayList<>();
        ArrayList<FluidStack> outputFluids = new ArrayList<>();

        /* INPUT */
        if (json.has(INGREDIENT)) {
            parseInputs(inputItems, inputFluids, json.get(INGREDIENT));
        } else if (json.has(INGREDIENTS)) {
            parseInputs(inputItems, inputFluids, json.get(INGREDIENTS));
        } else if (json.has(INPUT)) {
            parseInputs(inputItems, inputFluids, json.get(INPUT));
        } else if (json.has(INPUTS)) {
            parseInputs(inputItems, inputFluids, json.get(INPUTS));
        }

        /* OUTPUT */
        if (json.has(RESULT)) {
            parseOutputs(outputItems, outputItemChances, outputFluids, json.get(RESULT));
        } else if (json.has(RESULTS)) {
            parseOutputs(outputItems, outputItemChances, outputFluids, json.get(RESULTS));
        } else if (json.has(OUTPUT)) {
            parseOutputs(outputItems, outputItemChances, outputFluids, json.get(OUTPUT));
        } else if (json.has(OUTPUTS)) {
            parseOutputs(outputItems, outputItemChances, outputFluids, json.get(OUTPUTS));
        }

        /* ENERGY */
        if (json.has(ENERGY)) {
            energy = json.get(ENERGY).getAsInt();
        }
        if (json.has(ENERGY_MOD)) {
            energy *= json.get(ENERGY_MOD).getAsFloat();
        }
        energy = MathHelper.clamp(energy, 0, Integer.MAX_VALUE);

        /* XP */
        if (json.has(EXPERIENCE)) {
            experience = json.get(EXPERIENCE).getAsFloat();
        } else if (json.has(XP)) {
            experience = json.get(XP).getAsFloat();
        }
        if (inputItems.isEmpty() && inputFluids.isEmpty() || outputItems.isEmpty() && outputFluids.isEmpty() || energy <= 0) {
            throw new JsonSyntaxException("Invalid Thermal Series recipe! Please check your datapacks!");
        }
        return factory.create(energy, experience, inputItems, inputFluids, outputItems, outputItemChances, outputFluids);
    }

    protected JsonObject toJson(T recipe) {

        JsonObject json = new JsonObject();
        json.add(INPUTS, writeInputs(recipe.inputItems, recipe.inputFluids));
        json.addProperty(ENERGY, recipe.energy);
        json.addProperty(XP, recipe.xp);
        JsonArray outputs = new JsonArray();
        for (int i = 0; i < recipe.outputItems.size(); ++i) {
            JsonObject item = writeItemStack(recipe.outputItems.get(i));
            float chance = recipe.outputItemChances.get(i);
            item.addProperty(CHANCE, Math.abs(chance));
            if (chance < 0.0F) {
                item.addProperty(LOCKED, true);
            }
            outputs.add(item);
        }
        recipe.outputFluids.forEach(fluid -> outputs.add(writeFluidStack(fluid)));
        json.add(OUTPUTS, outputs);
        return json;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {

        return StreamCodec.of(this::toNetwork, this::fromNetwork);
    }

    private T fromNetwork(RegistryFriendlyByteBuf buffer) {

        int energy = buffer.readVarInt();
        float experience = buffer.readFloat();

        int numInputItems = buffer.readVarInt();
        ArrayList<Ingredient> inputItems = new ArrayList<>(numInputItems);
        for (int i = 0; i < numInputItems; ++i) {
            inputItems.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
        }

        int numInputFluids = buffer.readVarInt();
        ArrayList<FluidIngredient> inputFluids = new ArrayList<>(numInputFluids);
        for (int i = 0; i < numInputFluids; ++i) {
            inputFluids.add(FluidIngredient.fromNetwork(buffer));
        }

        int numOutputItems = buffer.readVarInt();
        ArrayList<ItemStack> outputItems = new ArrayList<>(numOutputItems);
        ArrayList<Float> outputItemChances = new ArrayList<>(numOutputItems);
        for (int i = 0; i < numOutputItems; ++i) {
            outputItems.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            outputItemChances.add(buffer.readFloat());
        }

        int numOutputFluids = buffer.readVarInt();
        ArrayList<FluidStack> outputFluids = new ArrayList<>(numOutputFluids);
        for (int i = 0; i < numOutputFluids; ++i) {
            outputFluids.add(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer));
        }
        if (inputItems.isEmpty() && inputFluids.isEmpty() || outputItems.isEmpty() && outputFluids.isEmpty()) {
            throw new JsonSyntaxException("Invalid Thermal Series recipe! Please check your datapacks!");
        }
        return factory.create(energy, experience, inputItems, inputFluids, outputItems, outputItemChances, outputFluids);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, T recipe) {

        buffer.writeVarInt(recipe.energy);
        buffer.writeFloat(recipe.xp);

        int numInputItems = recipe.inputItems.size();
        buffer.writeVarInt(numInputItems);
        for (int i = 0; i < numInputItems; ++i) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.inputItems.get(i));
        }
        int numInputFluids = recipe.inputFluids.size();
        buffer.writeVarInt(numInputFluids);
        for (int i = 0; i < numInputFluids; ++i) {
            recipe.inputFluids.get(i).toNetwork(buffer);
        }
        int numOutputItems = recipe.outputItems.size();
        buffer.writeVarInt(numOutputItems);
        for (int i = 0; i < numOutputItems; ++i) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.outputItems.get(i));
            buffer.writeFloat(recipe.outputItemChances.get(i));
        }
        int numOutputFluids = recipe.outputFluids.size();
        buffer.writeVarInt(numOutputFluids);
        for (int i = 0; i < numOutputFluids; ++i) {
            FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.outputFluids.get(i));
        }
    }

    public interface IFactory<T extends ThermalRecipe> {

        T create(int energy, float experience, List<Ingredient> inputItems, List<FluidIngredient> inputFluids, List<ItemStack> outputItems, List<Float> chance, List<FluidStack> outputFluids);

    }

}
