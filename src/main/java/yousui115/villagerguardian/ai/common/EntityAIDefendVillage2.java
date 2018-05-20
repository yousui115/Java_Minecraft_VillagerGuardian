package yousui115.villagerguardian.ai.common;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.village.Village;

public class EntityAIDefendVillage2 extends EntityAITarget
{
    EntityCreature taskOwner;

    /** The aggressor of the iron golem's village which is now the golem's attack target. */
    EntityLivingBase villageAgressorTarget;

    public EntityAIDefendVillage2(EntityCreature taskOwnerIn)
    {
        super(taskOwnerIn, false, true);
        this.taskOwner = taskOwnerIn;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        //■村が
        Village village = taskOwner.world.getVillageCollection().getNearestVillage(taskOwner.getPosition(), 32);

        //■無い
        if (village == null)
        {
            return false;
        }
        //■有る
        else
        {
            //■村が感知してる「最寄りの侵略者」を取得
            this.villageAgressorTarget = village.findNearestVillageAggressor(this.taskOwner);

            //■・・・爆発はちょっと。
            if (this.villageAgressorTarget instanceof EntityCreeper)
            {
                return false;
            }
            //■Suitable:適切な
            else if (this.isSuitableTarget(this.villageAgressorTarget, false))
            {
                return true;
            }
            //■抽選
            else if (this.taskOwner.getRNG().nextInt(20) == 0)
            {
                //■評判の悪いプレイヤー
                this.villageAgressorTarget = village.getNearestTargetPlayer(this.taskOwner);
                return this.isSuitableTarget(this.villageAgressorTarget, false);
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.villageAgressorTarget);
        super.startExecuting();
    }
}