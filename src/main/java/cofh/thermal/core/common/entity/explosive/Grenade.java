package cofh.thermal.core.common.entity.explosive;

import cofh.core.common.entity.AbstractGrenade;
import cofh.lib.api.IDetonatable;
import cofh.lib.util.Utils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static cofh.thermal.core.ThermalCore.ITEMS;

public class Grenade extends AbstractGrenade implements IDetonatable {

    protected IDetonateAction detonateAction;

    public Grenade(EntityType<? extends AbstractGrenade> type, Level worldIn, IDetonateAction detonateAction) {

        super(type, worldIn);
        this.detonateAction = detonateAction;
    }

    public Grenade(EntityType<? extends AbstractGrenade> type, Level worldIn, IDetonateAction detonateAction, double x, double y, double z) {

        super(type, x, y, z, worldIn);
        this.detonateAction = detonateAction;
    }

    public Grenade(EntityType<? extends AbstractGrenade> type, Level worldIn, IDetonateAction detonateAction, LivingEntity livingEntityIn) {

        super(type, livingEntityIn, worldIn);
        this.detonateAction = detonateAction;
    }

    @Override
    public void detonate(Vec3 pos) {

        detonateAction.detonate(level, this, getOwner(), pos, this.radius, this.effectDuration, this.effectAmplifier);
    }

    @Override
    public void setItem(ItemStack stack) {

        // A stackable grenade has no durability. Give its projectile a model marker instead.
        ItemStack thrown = stack.copyWithCount(1);
        thrown.remove(DataComponents.DAMAGE);
        thrown.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        super.setItem(thrown);
    }

    @Override
    protected Item getDefaultItem() {

        return ITEMS.get(Utils.getRegistryName(getType()));
    }

}
