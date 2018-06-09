package yousui115.villagerguardian.ai.witch;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAINearestHealTarget <T extends EntityLivingBase> extends EntityAITarget
{
    protected final Class<T> targetClass;
    private final int targetChance;
    /** Instance of EntityAINearestAttackableTargetSorter. */
    protected final EntityAINearestHealTarget.Sorter sorter;
    protected final Predicate <? super T > targetEntitySelector;
    protected T targetEntity;

    public EntityAINearestHealTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight)
    {
        this(creature, classTarget, checkSight, false);
    }

    public EntityAINearestHealTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby)
    {
        this(creature, classTarget, 10, checkSight, onlyNearby, (Predicate)null);
    }

    public EntityAINearestHealTarget(EntityCreature creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate <? super T > targetSelector)
    {
        super(creature, checkSight, onlyNearby);
        this.targetClass = classTarget;
        this.targetChance = chance;
        this.sorter = new EntityAINearestHealTarget.Sorter(creature);
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<T>()
        {
            public boolean apply(@Nullable T p_apply_1_)
            {
                if (p_apply_1_ == null)
                {
                    return false;
                }
//                else if (EntityAINearestHealTarget.this.taskOwner.getEntitySenses().canSee(p_apply_1_) == false)
//                {
//                    return false;
//                }
                else if (targetSelector != null && targetSelector.apply(p_apply_1_) == false)
                {
                    return false;
                }
                else if (needHealTarget(p_apply_1_) == true)
                {
                    if (EntityAINearestHealTarget.this.taskOwner.getNavigator().getPathToEntityLiving(p_apply_1_) == null)
                    {
                        return false;
                    }
                    return true;
                }
//                else
//                {
//                    return !EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : EntityAINearestHealTarget.this.isSuitableTarget(p_apply_1_, false);
//                }

                return false;
            }
        };
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0)
        {
            return false;
        }
        else if (this.targetClass != EntityPlayer.class && this.targetClass != EntityPlayerMP.class)
        {
            List<T> list = this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);

            if (list.isEmpty())
            {
                return false;
            }
            else
            {
                Collections.sort(list, this.sorter);
                this.targetEntity = list.get(0);
                return true;
            }
        }
        else
        {
            this.targetEntity = (T)this.taskOwner.world.getNearestAttackablePlayer(this.taskOwner.posX, this.taskOwner.posY + (double)this.taskOwner.getEyeHeight(), this.taskOwner.posZ, this.getTargetDistance(), this.getTargetDistance(), new Function<EntityPlayer, Double>()
            {
                @Nullable
                public Double apply(@Nullable EntityPlayer p_apply_1_)
                {
                    ItemStack itemstack = p_apply_1_.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                    if (itemstack.getItem() == Items.SKULL)
                    {
                        int i = itemstack.getItemDamage();
                        boolean flag = EntityAINearestHealTarget.this.taskOwner instanceof EntitySkeleton && i == 0;
                        boolean flag1 = EntityAINearestHealTarget.this.taskOwner instanceof EntityZombie && i == 2;
                        boolean flag2 = EntityAINearestHealTarget.this.taskOwner instanceof EntityCreeper && i == 4;

                        if (flag || flag1 || flag2)
                        {
                            return 0.5D;
                        }
                    }

                    return 1.0D;
                }
            }, (Predicate<EntityPlayer>)this.targetEntitySelector);
            return this.targetEntity != null;
        }
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        if (super.shouldContinueExecuting() == true)
        {
            return needHealTarget(targetEntity);
        }

        return false;
    }

    public boolean needHealTarget(T targetIn)
    {
        //■「HPが全快ではない かつ リジェネ無し」 または 「燃えてる かつ レジスタンス無し」
//        boolean flag0 = targetIn.getHealth() < targetIn.getMaxHealth();
//        boolean flag1 = targetIn.getHealth() < targetIn.getMaxHealth() * 4 / 5 || targetIn.isPotionActive(MobEffects.REGENERATION) == false;
        boolean flag1 = targetIn.getHealth() < targetIn.getMaxHealth() && targetIn.isPotionActive(MobEffects.REGENERATION) == false;
        boolean flag2 = targetIn.isBurning() && targetIn.isPotionActive(MobEffects.FIRE_RESISTANCE) == false;
//        return flag0 && (flag1 || flag2);
        return flag1 || flag2;
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance)
    {
        return this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntity);
        this.target = this.targetEntity;
        super.startExecuting();
    }

    public static class Sorter implements Comparator<Entity>
        {
            private final Entity entity;

            public Sorter(Entity entityIn)
            {
                this.entity = entityIn;
            }

            public int compare(Entity p_compare_1_, Entity p_compare_2_)
            {
                double d0 = this.entity.getDistanceSq(p_compare_1_);
                double d1 = this.entity.getDistanceSq(p_compare_2_);

                if (d0 < d1)
                {
                    return -1;
                }
                else
                {
                    return d0 > d1 ? 1 : 0;
                }
            }
        }
}
