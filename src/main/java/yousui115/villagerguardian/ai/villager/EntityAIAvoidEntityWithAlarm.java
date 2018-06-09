package yousui115.villagerguardian.ai.villager;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import yousui115.villagerguardian.util.Utils;

public class EntityAIAvoidEntityWithAlarm<T extends Entity> extends EntityAIAvoidEntity
{
    //■絶え間なく叫ぶと喉が枯れちゃうので。
    protected int cooltime = 0;


    /**
     * ■コンストラクタ
     */
    public EntityAIAvoidEntityWithAlarm(EntityCreature entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
    {
        this(entityIn, classToAvoidIn, Predicates.alwaysTrue(), avoidDistanceIn, farSpeedIn, nearSpeedIn);
    }
    public EntityAIAvoidEntityWithAlarm(EntityCreature entityIn, Class<T> classToAvoidIn, Predicate <? super T > avoidTargetSelectorIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
    {
        super(entityIn, classToAvoidIn, avoidTargetSelectorIn, avoidDistanceIn, farSpeedIn, nearSpeedIn);
    }

    /**
     * ■
     */
    @Override
    public void updateTask()
    {
        super.updateTask();

        if (this.closestLivingEntity instanceof EntityLivingBase == false ||
            this.closestLivingEntity.isEntityAlive() == false)
        {
            return;
        }

        //■誰かー！
        if (cooltime-- <= 0)
        {
            //■近くのVGへ向けて叫ぶ
            List<EntityCreature> listVG = entity.world.getEntitiesWithinAABB(EntityCreature.class, entity.getEntityBoundingBox().grow(10d), new Predicate<EntityCreature>()
                                                {
                                                    @Override
                                                    public boolean apply(EntityCreature target)
                                                    {
                                                        if (Utils.isGuardian(target) == true)
                                                        {
                                                            return true;
                                                        }

                                                        return false;
                                                    }
                                                });

            //■近くにVGが居た！
            if (listVG.size() != 0)
            {
                //Debug
//                System.out.println("Help!");

                for (EntityCreature vg : listVG)
                {
                    //■ターゲットが居ないなら おたすけー！
                    if (vg.getAttackTarget() == null || vg.getAttackTarget().isEntityAlive() == false)
                    {
                        //Debug
//                        System.out.println("OK!");

                        vg.setAttackTarget((EntityLivingBase)this.closestLivingEntity);

                        //TODO これはいらんかも。
                        vg.setRevengeTarget((EntityLivingBase)this.closestLivingEntity);

                        //TODO VG一体に通知したら、フラグを立てて、他のVGには通知しないようにするべきか否か。

                    }
                }
            }

            cooltime = 20;
        }

    }
}
