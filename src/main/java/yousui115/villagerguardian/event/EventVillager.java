package yousui115.villagerguardian.event;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yousui115.villagerguardian.ai.villager.EntityAINegotiationMob;

public class EventVillager
{
    @SubscribeEvent(priority = EventPriority.LOW)
    public void joinWorldVillager(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityVillager == false) { return; }
        EntityVillager villager = (EntityVillager)event.getEntity();

        villager.tasks.addTask(5, new EntityAINegotiationMob(villager));

    }
}
