package cofh.thermal.lib.common.block.entity;

import cofh.core.common.block.entity.BlockEntityCoFH;
import cofh.core.util.control.IRedstoneControllableTile;
import cofh.core.util.control.ISecurableTile;
import cofh.core.util.control.RedstoneControlModule;
import cofh.core.util.control.SecurityControlModule;
import cofh.thermal.core.common.config.ThermalCoreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


public class LogisticsBlockEntity extends BlockEntityCoFH implements ISecurableTile, IRedstoneControllableTile {

    protected SecurityControlModule securityControl = new SecurityControlModule(this);
    protected RedstoneControlModule redstoneControl = new RedstoneControlModule(this);

    public LogisticsBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {

        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public ItemStack createItemStackTag(ItemStack stack) {

        CompoundTag nbt = copyBlockEntityData(stack);
        if (hasSecurity()) {
            securityControl().write(nbt);
        }
        if (ThermalCoreConfig.keepRSControl.get()) {
            redstoneControl().writeSettings(nbt);
        }
        if (!nbt.isEmpty()) {
            BlockItem.setBlockEntityData(stack, getType(), nbt);
        }
        return super.createItemStackTag(stack);
    }

    // region NBT
    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {

        super.loadAdditional(nbt, registries);

        securityControl.read(nbt);
        redstoneControl.read(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {

        super.saveAdditional(nbt, registries);

        securityControl.write(nbt);
        redstoneControl.write(nbt);
    }
    // endregion

    // region NETWORK

    // CONTROL
    @Override
    public RegistryFriendlyByteBuf getControlPacket(RegistryFriendlyByteBuf buffer) {

        super.getControlPacket(buffer);

        securityControl.writeToBuffer(buffer);
        redstoneControl.writeToBuffer(buffer);

        return buffer;
    }

    @Override
    public void handleControlPacket(RegistryFriendlyByteBuf buffer) {

        super.handleControlPacket(buffer);

        securityControl.readFromBuffer(buffer);
        redstoneControl.readFromBuffer(buffer);
    }
    // endregion

    // region MODULES
    @Override
    public SecurityControlModule securityControl() {

        return securityControl;
    }

    @Override
    public RedstoneControlModule redstoneControl() {

        return redstoneControl;
    }
    // endregion

    // region IConveyableData
    @Override
    public void readConveyableData(Player player, CompoundTag tag) {

        redstoneControl.readSettings(tag);

        onControlUpdate();
    }

    @Override
    public void writeConveyableData(Player player, CompoundTag tag) {

        redstoneControl.writeSettings(tag);
    }
    // endregion
}
