package yousui115.villagerguardian.ai.villager;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.Path;
import yousui115.villagerguardian.Utils;

public class EntityAINegotiationMob extends EntityAIBase
{
    private EntityVillager taskOwner;

    private Path path;

    private EntityCreature targetVG;

    private int tick;

    public EntityAINegotiationMob(EntityVillager ownerIn)
    {
        taskOwner = ownerIn;
    }

    @Override
    public boolean shouldExecute()
    {
        //■低確率！
        if (taskOwner.getRNG().nextInt(1000) != 0) { return false; }

        //■必ずインスタンスが生成されて帰ってくる
        List<EntityCreature> VGs = taskOwner.world.<EntityCreature>getEntitiesWithinAABB(EntityCreature.class, taskOwner.getEntityBoundingBox().grow(6.0D, 2.0D, 6.0D), new Predicate<EntityCreature>()
        {
            @Override
            public boolean apply(EntityCreature target)
            {
                if (target != null && target.isEntityAlive() == true)
                {
                    UUID uuid = Utils.getPairUUID(target);

                    //■未契約 かつ 村人護り隊である かつ 扉を通れる。
                    if (uuid == null && Utils.isGuardian(target) == true && Utils.canDoorThrough(target) == true)
                    {
                        return true;
                    }
                }
                return false;
            }
        });

        //■ひとりで行ききれるの？
        for (EntityCreature vg : VGs)
        {
            path = taskOwner.getNavigator().getPathToEntityLiving(vg);

            if (path != null)
            {
                targetVG = vg;
                return true;
            }
        }

        return false;
    }

    @Override
    public void startExecuting()
    {
        tick = 0;
        taskOwner.getNavigator().setPath(path, 1.0d);
    }


    @Override
    public boolean shouldContinueExecuting()
    {
        return tick < 40 || (path != null && path.isFinished() == false);
    }

    @Override
    public void resetTask()
    {
        //■交渉成立
        if (targetVG != null && targetVG.isEntityAlive() == true &&
            taskOwner.getDistance(targetVG) < 2f)
        {
            //■VGに自分のUUIDを設定する。
            Utils.setPairUUID(targetVG, taskOwner.getUniqueID());

            //■フッフーン！
            taskOwner.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

            //■ハートのパーティクル
            taskOwner.world.setEntityState(taskOwner, (byte)12);
        }

        tick = 0;
        path = null;
        targetVG = null;
    }

    @Override
    public void updateTask()
    {
        if (path != null && path.isFinished() == true)
        {
            tick++;
        }
    }
}
