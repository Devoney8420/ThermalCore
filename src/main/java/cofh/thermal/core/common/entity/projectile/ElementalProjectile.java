package cofh.thermal.core.common.entity.projectile;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class ElementalProjectile extends AbstractHurtingProjectile {

    public ElementalProjectile(EntityType<? extends AbstractHurtingProjectile> type, Level world) {

        super(type, world);
    }

    public ElementalProjectile(EntityType<? extends AbstractHurtingProjectile> type, LivingEntity shooter, double accelX, double accelY, double accelZ, Level world) {

        super(type, shooter, new Vec3(accelX, accelY, accelZ), world);
    }

    public ElementalProjectile(EntityType<? extends AbstractHurtingProjectile> type, double x, double y, double z, double accelX, double accelY, double accelZ, Level world) {

        super(type, x, y, z, new Vec3(accelX, accelY, accelZ), world);
    }

    @Override
    protected boolean shouldBurn() {

        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {

        return false;
    }

    @Override
    public float getPickRadius() {

        return 0.0F;
    }

    @Override
    public boolean isPickable() {

        return false;
    }

    // region HELPERS
    protected DamageSource damageSource() {

        Entity owner = getOwner();
        return this.level.damageSources().source(getDamageType(), this, owner == null ? this : owner);
    }

    protected abstract ResourceKey<DamageType> getDamageType();

    protected abstract float getDamage(Entity target);

    protected int getEffectAmplifier(Entity target) {

        return 0;
    }

    protected int getEffectDuration(Entity target) {

        return 100;
    }
    // endregion
}
