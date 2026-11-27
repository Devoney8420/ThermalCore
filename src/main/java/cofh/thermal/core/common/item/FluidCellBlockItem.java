package cofh.thermal.core.common.item;

import cofh.core.util.helpers.AugmentDataHelper;
import cofh.core.util.helpers.FluidHelper;
import cofh.lib.api.item.IFluidContainerItem;
import cofh.lib.util.helpers.StackData;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermal.core.common.block.entity.storage.FluidCellBlockEntity;
import cofh.thermal.lib.common.item.BlockItemAugmentable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.helpers.AugmentableHelper.getPropertyWithDefault;
import static cofh.core.util.helpers.AugmentableHelper.setAttributeFromAugmentMax;
import static cofh.core.util.helpers.FluidHelper.addPotionTooltip;
import static cofh.lib.api.ContainerType.FLUID;
import static cofh.lib.util.constants.NBTTags.*;
import static cofh.lib.util.helpers.StringHelper.*;
import static net.minecraft.nbt.Tag.TAG_COMPOUND;

public class FluidCellBlockItem extends BlockItemAugmentable implements IFluidContainerItem {

    public FluidCellBlockItem(Block blockIn, Properties builder) {

        super(blockIn, builder);

        setEnchantability(5);
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(StringHelper.getFluidName(fluid));
        }
        tooltip.add(isCreative(stack, FLUID)
                ? getTextComponent("info.cofh.infinite").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC)
                : getTextComponent(localize("info.cofh.amount") + ": " + format(fluid.getAmount()) + " / " + format(getCapacity(stack)) + " " + localize("info.cofh.unit_mb")));

        if (FluidHelper.hasPotionTag(fluid)) {
            tooltip.add(getEmptyLine());
            tooltip.add(getTextComponent(localize("info.cofh.effects") + ":"));
            addPotionTooltip(fluid, tooltip);
        }
    }

    private void setAttributesFromAugment(CompoundTag properties, CompoundTag augmentData) {

        setAttributeFromAugmentMax(properties, augmentData, TAG_AUGMENT_BASE_MOD);
        setAttributeFromAugmentMax(properties, augmentData, TAG_AUGMENT_FLUID_STORAGE);
        setAttributeFromAugmentMax(properties, augmentData, TAG_AUGMENT_FLUID_CREATIVE);
    }

    @Override
    public FluidStack getFluid(ItemStack container) {

        FluidStack stored = IFluidContainerItem.super.getFluid(container);
        if (!stored.isEmpty()) {
            return stored;
        }
        CompoundTag blockData = container.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        ListTag tanks = blockData.getList(TAG_TANK_INV, TAG_COMPOUND);
        return tanks.isEmpty() ? FluidStack.EMPTY : StackData.readFluid(StackData.builtInRegistries(), tanks.getCompound(0));
    }

    @Override
    public void setFluid(ItemStack container, FluidStack fluid) {

        IFluidContainerItem.super.setFluid(container, fluid);
        CustomData blockData = container.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockData != null && blockData.contains(TAG_TANK_INV)) {
            CustomData.update(DataComponents.BLOCK_ENTITY_DATA, container, tag -> tag.remove(TAG_TANK_INV));
        }
    }

    @Override
    public int getCapacity(ItemStack container) {

        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_FLUID_STORAGE, 1.0F);
        return getMaxStored(container, Math.round(FluidCellBlockEntity.BASE_CAPACITY * mod * base));
    }
    // region IAugmentableItem
    @Override
    public void updateAugmentState(ItemStack container, List<ItemStack> augments) {

        CompoundTag properties = new CompoundTag();
        for (ItemStack augment : augments) {
            CompoundTag augmentData = AugmentDataHelper.getAugmentData(augment);
            if (augmentData == null) {
                continue;
            }
            setAttributesFromAugment(properties, augmentData);
        }
        CustomData.update(DataComponents.CUSTOM_DATA, container, tag -> tag.put(TAG_PROPERTIES, properties));
        FluidStack stored = getFluid(container);
        if (stored.getAmount() > getCapacity(container)) {
            setFluid(container, stored.copyWithAmount(getCapacity(container)));
        }
    }
    // endregion
}
