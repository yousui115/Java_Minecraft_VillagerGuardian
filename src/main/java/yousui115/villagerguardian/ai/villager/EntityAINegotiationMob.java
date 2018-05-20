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

    private EntityCreature target;

    private int tick;

    public EntityAINegotiationMob(EntityVillager ownerIn)
    {
        taskOwner = ownerIn;
    }

    @Override
    public boolean shouldExecute()
    {
        //■低確率！
        if (taskOwner.getRNG().nextInt(500) != 0) { return false; }

        //■必ずインスタンスが生成されて帰ってくる
        List<EntityCreature> entities = taskOwner.world.<EntityCreature>getEntitiesWithinAABB(EntityCreature.class, taskOwner.getEntityBoundingBox().grow(6.0D, 2.0D, 6.0D), new Predicate<EntityCreature>()
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
        for (EntityCreature cre : entities)
        {
            path = taskOwner.getNavigator().getPathToEntityLiving(cre);

            if (path != null)
            {
                target = cre;
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
        if (target != null && target.isEntityAlive() == true &&
            taskOwner.getDistance(target) < 2f)
        {
            Utils.setPairUUID(target, taskOwner.getUniqueID());

//            taskOwner.spawnExplosionParticle();
//            taskOwner.playSound(SoundEvents.ENTITY_GENERIC_DEATH, 1.0f, 1.0f);

            taskOwner.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

//            taskOwner.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, taskOwner.posX + (double)(taskOwner.getRNG().nextFloat() * taskOwner.width * 2.0F) - (double)taskOwner.width, taskOwner.posY + 0.5D + (double)(taskOwner.getRNG().nextFloat() * taskOwner.height), taskOwner.posZ + (double)(taskOwner.getRNG().nextFloat() * taskOwner.width * 2.0F) - (double)taskOwner.width, 0.0D, 0.0D, 0.0D);

            taskOwner.world.setEntityState(taskOwner, (byte)12);

//            taskOwner.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 40, 0));
//            for (int i = 0; i < 5; ++i)
//            {
//                Random rand = target.getRNG();
//
//                double d0 = rand.nextGaussian() * 0.02D;
//                double d1 = rand.nextGaussian() * 0.02D;
//                double d2 = rand.nextGaussian() * 0.02D;
//
//                Utils.setPairUUID(target, taskOwner.getUniqueID());
//                target.world.spawnParticle(EnumParticleTypes.HEART, target.posX + (double)(target.getRNG().nextFloat() * target.width * 2.0F) - (double)target.width,
//                                                                    target.posY + 1.0D + (double)(rand.nextFloat() * target.height),
//                                                                    target.posZ + (double)(rand.nextFloat() * target.width * 2.0F) - (double)target.width,
//                                                                    d0, d1, d2);
//            }
        }

        tick = 0;
        path = null;
        target = null;
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
