package yousui115.villagerguardian.ai.common;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.village.Village;

public class EntityAIFollowPlayerToVillage extends EntityAIBase
{
    //■オーナー
    private EntityCreature taskOwner;

    //■引率プレイヤー
    private EntityPlayer player;

    //■村
    private Village village;

    //■村感知範囲
    private int villageDist = 0;

    //■きょろきょろディレイ
    private int delay = 0;

    private int matteTime = 0;
    /**
     *
     * @param ownerIn
     */
    public EntityAIFollowPlayerToVillage(EntityCreature ownerIn)
    {
        taskOwner = ownerIn;
        this.setMutexBits(~0x100);
    }

    @Override
    public boolean shouldExecute()
    {
        if (delay > 0) { return true; }

        if (player != null && isAttackTargetPlayer(taskOwner, player) == true)
        {
            return false;
        }

        //■村はどこぞ
        village = taskOwner.world.getVillageCollection().getNearestVillage(taskOwner.getPosition(), villageDist);
        if (village != null)
        {
            //■あった。プレイヤーさん、さよなら。
            setHomeVillage(village);
            if (player != null)
            {
                player.sendMessage(new TextComponentTranslation(taskOwner.getName() + " : " + I18n.translateToLocal("thanks")));
                player = null;
            }

            return false;
        }

        if (player == null)
        {
            //■ちょっとそこのプレイヤーさん、ひとつ村まで。
            List<EntityPlayer> list = taskOwner.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, taskOwner.getEntityBoundingBox().grow(6.0D, 2.0D, 6.0D));
            if (list.size() != 0)
            {
                player = list.get(0);
                if (isAttackTargetPlayer(taskOwner, player) == true) { return false; }

                player.sendMessage(new TextComponentTranslation(taskOwner.getName() + " : " + I18n.translateToLocal("hello")));
            }
        }

        return player != null;
    }

    public boolean isAttackTargetPlayer(EntityCreature creatureIn, EntityPlayer playerIn)
    {
        if (creatureIn != null && player != null)
        {
            if (creatureIn.getAttackTarget() != null)
            {
                if (playerIn.getUniqueID().equals(taskOwner.getAttackTarget().getUniqueID()) == true)
                {
                    return true;
                }
            }
        }

        return false;
    }
    @Override
    public boolean shouldContinueExecuting()
    {
        return this.shouldExecute();
    }

    @Override
    public void startExecuting()
    {
        delay = 20;
    }

    @Override
    public void resetTask()
    {
        delay = 0;
        player = null;
    }

    @Override
    public void updateTask()
    {
        delay = --delay >= 0 ? delay : 20;
        matteTime =  --matteTime < 0 ? 0 : matteTime;

        if (player == null)
        {
            return;
        }
        else if (player.isEntityAlive() == false)
        {
            player = null;
            return;
        }

        //■プレイヤーについていく
        if (taskOwner.getDistanceSq(this.player) < 4.0D)
        {
            taskOwner.getNavigator().clearPath();
        }
        //■ちょっと遠い。
        else
        {
            if (taskOwner.getNavigator().tryMoveToEntityLiving(this.player, 1.0D) == false)
            {
                if (matteTime == 0)
                {
                    matteTime = 100;
                    player.sendMessage(new TextComponentTranslation(taskOwner.getName() + " : " + I18n.translateToLocal("wait")));
                }
            }
        }
    }


    private void setHomeVillage(@Nonnull Village villageIn)
    {
        taskOwner.setHomePosAndDistance(villageIn.getCenter(), (int)((float)villageIn.getVillageRadius() * 0.6F));
    }
}
