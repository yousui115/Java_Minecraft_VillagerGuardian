package yousui115.villagerguardian.ai.witch;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.MathHelper;

public class EntityAIHealRanged extends EntityAIBase
{
    /** The entity the AI instance has been applied to */
    private final EntityWitch taskOwner;
    /** The entity (as a RangedAttackMob) the AI instance has been applied to. */
    private final EntityWitch rangedAttackEntityHost;
    private EntityLivingBase attackTarget;
    /**
     * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
     * maxRangedAttackTime.
     */
    private int rangedAttackTime;
    private final double entityMoveSpeed;
    private int seeTime;
    private final int attackIntervalMin;
    /** The maximum time the AI has to wait before peforming another ranged attack. */
    private final int maxRangedAttackTime;
    private final float attackRadius;
    private final float maxAttackDistance;

    public EntityAIHealRanged(EntityWitch witchIn, double movespeed, int maxAttackTime, float maxAttackDistanceIn)
    {
        this(witchIn, movespeed, maxAttackTime, maxAttackTime, maxAttackDistanceIn);
    }

    public EntityAIHealRanged(EntityWitch witchIn, double movespeed, int p_i1650_4_, int maxAttackTime, float maxAttackDistanceIn)
    {
        this.rangedAttackTime = -1;

        if (!(witchIn instanceof EntityLivingBase))
        {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        else
        {
            this.rangedAttackEntityHost = witchIn;
            this.taskOwner = witchIn;
            this.entityMoveSpeed = movespeed;
            this.attackIntervalMin = p_i1650_4_;
            this.maxRangedAttackTime = maxAttackTime;
            this.attackRadius = maxAttackDistanceIn;
            this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
            this.setMutexBits(3);
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = this.taskOwner.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else
        {
            this.attackTarget = entitylivingbase;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return this.shouldExecute() || !this.taskOwner.getNavigator().noPath();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        this.attackTarget = null;
        this.seeTime = 0;
        this.rangedAttackTime = -1;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        double d0 = this.taskOwner.getDistanceSq(this.attackTarget.posX, this.attackTarget.getEntityBoundingBox().minY, this.attackTarget.posZ);
        boolean flag = this.taskOwner.getEntitySenses().canSee(this.attackTarget);

        if (flag)
        {
            ++this.seeTime;
        }
        else
        {
            this.seeTime = 0;
        }

        if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20)
        {
            this.taskOwner.getNavigator().clearPath();
        }
        else
        {
            this.taskOwner.getNavigator().tryMoveToEntityLiving(this.attackTarget, this.entityMoveSpeed);
        }

        this.taskOwner.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);

        if (--this.rangedAttackTime == 0)
        {
            if (!flag)
            {
                return;
            }

            float f = MathHelper.sqrt(d0) / this.attackRadius;
            float lvt_5_1_ = MathHelper.clamp(f, 0.1F, 1.0F);


//            this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, lvt_5_1_);
            healEntityWithRanged(this.attackTarget, lvt_5_1_);

            this.rangedAttackTime = MathHelper.floor(f * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin);
        }
        else if (this.rangedAttackTime < 0)
        {
            float f2 = MathHelper.sqrt(d0) / this.attackRadius;
            this.rangedAttackTime = MathHelper.floor(f2 * (float)(this.maxRangedAttackTime - this.attackIntervalMin) + (float)this.attackIntervalMin);
        }
    }

    protected void healEntityWithRanged(EntityLivingBase targetIn, float distanceFactorIn)
    {
        if (!taskOwner.isDrinkingPotion())
        {
            //■射角の算出
            double d0 = targetIn.posY + (double)targetIn.getEyeHeight() - 1.100000023841858D;
            double d2 = d0 - taskOwner.posY;

            //■方向の算出
            double d1 = targetIn.posX + targetIn.motionX - taskOwner.posX;
            double d3 = targetIn.posZ + targetIn.motionZ - taskOwner.posZ;

            //■距離
            float f = MathHelper.sqrt(d1 * d1 + d3 * d3);

            PotionType potiontype = PotionTypes.HARMING;

            if (targetIn.isBurning() == true && targetIn.isPotionActive(MobEffects.FIRE_RESISTANCE) == false)
            {
                potiontype = PotionTypes.FIRE_RESISTANCE;
            }
            else if (targetIn.getHealth() < targetIn.getMaxHealth() * 4 / 5)
            {
                potiontype = PotionTypes.STRONG_HEALING;
            }
            else
            {
                potiontype = PotionTypes.LONG_REGENERATION;
            }
//            if (f >= 8.0F && !targetIn.isPotionActive(MobEffects.SLOWNESS))
//            {
//                potiontype = PotionTypes.SLOWNESS;
//            }
//            else if (targetIn.getHealth() >= 8.0F && !targetIn.isPotionActive(MobEffects.POISON))
//            {
//                potiontype = PotionTypes.POISON;
//            }
//            else if (f <= 3.0F && !targetIn.isPotionActive(MobEffects.WEAKNESS) && taskOwner.getRNG().nextFloat() < 0.25F)
//            {
//                potiontype = PotionTypes.WEAKNESS;
//            }

            EntityPotion entitypotion = new EntityPotion(taskOwner.world, taskOwner, PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potiontype));
            entitypotion.rotationPitch -= -20.0F;
            entitypotion.shoot(d1, d2 + (double)(f * 0.2F), d3, 0.75F, 8.0F);
            taskOwner.world.playSound((EntityPlayer)null, taskOwner.posX, taskOwner.posY, taskOwner.posZ, SoundEvents.ENTITY_WITCH_THROW, taskOwner.getSoundCategory(), 1.0F, 0.8F + taskOwner.getRNG().nextFloat() * 0.4F);
            taskOwner.world.spawnEntity(entitypotion);
        }
    }
}