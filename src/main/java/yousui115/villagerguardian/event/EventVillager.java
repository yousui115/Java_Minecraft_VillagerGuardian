package yousui115.villagerguardian.event;

import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yousui115.villagerguardian.ai.villager.EntityAIAvoidEntityWithAlarm;
import yousui115.villagerguardian.ai.villager.EntityAINegotiationMob;

public class EventVillager
{
    /**
     * ■おぎゃー
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void joinWorldVillager(EntityJoinWorldEvent event)
    {
        //■村人のみ
        if (event.getEntity() instanceof EntityVillager == false) { return; }
        EntityVillager villager = (EntityVillager)event.getEntity();

        //■逃げてる最中に近くのVGに助けを求める。（生成時なので、AI処理は開始してないはず）
        Object[] sets = villager.tasks.taskEntries.toArray();
        for (int i = 0; i < sets.length; i++)
        {
            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = (EntityAITasks.EntityAITaskEntry)sets[i];

            int priority = entityaitasks$entityaitaskentry.priority;
            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;

            //■挿げ替える
            if (entityaibase instanceof EntityAIAvoidEntity)
            {
                //Class<T> classToAvoid を取得
                Class clazz = (Class)ObfuscationReflectionHelper.getPrivateValue(EntityAIAvoidEntity.class, (EntityAIAvoidEntity)entityaibase, 8);

                //■必要な情報は取得したので削除
                villager.tasks.removeTask(entityaibase);

                //TODO めんどい事に、対象によってパラメータが異なる。が、今は無視。
                //TODO コンストラクタ内で例外が発生するかもしれないリフレクションはちょっと。
                entityaibase = new EntityAIAvoidEntityWithAlarm(villager, clazz, 8.0F, 0.6D, 0.6D);

                //■追加
                villager.tasks.addTask(priority, entityaibase);
            }

        }

        //■VGと交渉
        villager.tasks.addTask(5, new EntityAINegotiationMob(villager));

    }
}
