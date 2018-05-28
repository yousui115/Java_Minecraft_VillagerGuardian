package yousui115.villagerguardian.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yousui115.villagerguardian.util.Configs;
import yousui115.villagerguardian.util.Utils;

public class EventCreature
{
    /**
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void joinWorldCreature(EntityJoinWorldEvent event)
    {
        //■対象者
        if (event.getEntity() instanceof EntityCreature == false) { return; }
        EntityCreature mob = (EntityCreature)event.getEntity();

        //■入隊条件
//        if (Utils.canChangeVillagerDefender(mob) == false) { return; }

        //■DataParamの登録(必須)
        //  (内部でカスタムデータからの読み込みも行ってる)
        Utils.registerParam(mob);

        //■村人護り隊員(自然発生アイアンゴーレムは無条件)
        boolean isVillageGolem = mob instanceof EntityIronGolem && ((EntityIronGolem)mob).isPlayerCreated() == false;
        if (Utils.isGuardian(mob) == true || isVillageGolem)
        {
            //■再教育
            Utils.reeducationAI(mob);
        }
    }

    /**
     * ■アップデートイベント
     * @param event
     */
    @SubscribeEvent
    public void updateCreature(LivingUpdateEvent event)
    {
        //■クリーチャーが対象
        if (event.getEntity() instanceof EntityCreature == false) { return; }
        EntityCreature mob = (EntityCreature)event.getEntity();

        //■攻撃対象が「村人護り隊」隊員なら攻撃をやめる
        if (mob.getAttackTarget() instanceof EntityCreature &&
            Utils.isGuardian((EntityCreature)mob.getAttackTarget()) == true)
        {
            mob.setAttackTarget(null);
        }

        //■座り込んじゃう対策
        if (Utils.isGuardian(mob) == true &&
            mob instanceof EntityTameable && ((EntityTameable)mob).isSitting() == true)
        {
            ((EntityTameable)mob).setSitting(false);
        }
    }

    /**
     * ■右クリック！
     * @param event
     */
    @SubscribeEvent
    public void interactMob(PlayerInteractEvent.EntityInteract event)
    {
        //■サーバーのみ
        if (event.getWorld().isRemote) { return; }

        //■メインハンド に
        if (event.getHand() != EnumHand.MAIN_HAND) { return; }

        //■アレ を持っている
        if (event.getEntityPlayer().getHeldItemMainhand().getItem() != Items.EMERALD) { return; }

        //■対象者
        if (event.getTarget() instanceof EntityCreature == false) { return; }
        EntityCreature mob = (EntityCreature)event.getTarget();

        //■村人護り隊員は除外
        if (Utils.isGuardian(mob) == true) { return; }

        //■対象資格があるか
        if (Utils.canChangeVillagerDefender(mob) == false) { return; }

        //■一個使う
        event.getEntityPlayer().getHeldItemMainhand().shrink(1);

        //■腕を振る。
        event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);

        //■お返事。
        mob.playLivingSound();

        //■村人護り隊に入隊します！
        Utils.setIsGuardian(mob, true);
        Utils.setPairUUID(mob, null);
        Utils.reeducationAI(mob);
    }


    //=========================== Debug ===================================


    /**
     * ■ダメージ告知
     * @param event
     */
    @SubscribeEvent
    public void noticeDamageVG(LivingAttackEvent event)
    {
        if (Configs.getIsNoticeDamageMessage() == false) { return; }

        if (event.getEntityLiving() != null && event.getEntity().world.isRemote == true) {return;}

        //■村人護り隊 が攻撃された！
        if (event.getEntityLiving() instanceof EntityCreature == false) { return; }
        EntityCreature mob = (EntityCreature)event.getEntityLiving();
        if (Utils.isGuardian(mob) == false) { return; }

        //■相手は？(Entityからの攻撃以外は表示しない)
        Entity trueSrc = event.getSource().getTrueSource();
        if (trueSrc == null) { return; }
        boolean isG = false;
        if (trueSrc instanceof EntityCreature && Utils.isGuardian((EntityCreature)trueSrc) == true) { isG = true; }
//        if (trueSrc instanceof EntityCreature == false || Utils.isGuardian((EntityCreature)trueSrc) == false) { return; }

        //■VG <- VG への攻撃が表示される。
        String str = "Damage : " + mob.getName() + " [VG] <- " + trueSrc.getName();
        str = str + (isG == true ? " [VG]" : " ");

        for (EntityPlayer player : mob.world.playerEntities)
        {
            player.sendMessage(new TextComponentTranslation(str));
        }
    }

    @SubscribeEvent
    public void noticeDeathVG(LivingDeathEvent event)
    {
        if (Configs.getIsNoticeDeathMessage() == false) { return; }

        if (event.getEntityLiving() != null && event.getEntity().world.isRemote == true) {return;}

        //■村人護り隊 が攻撃された！
        if (event.getEntityLiving() instanceof EntityCreature == false) { return; }
        EntityCreature mob = (EntityCreature)event.getEntityLiving();
        if (Utils.isGuardian(mob) == false) { return; }

        //■相手は？
        Entity trueSrc = event.getSource().getTrueSource();
//        if (trueSrc instanceof EntityCreature == false || Utils.isGuardian((EntityCreature)trueSrc) == false) { return; }

        //■VG <- VG への攻撃が表示される。
        String str = "Death : " + mob.getName() + " <- " + (trueSrc == null ? "null" : trueSrc.getName());

        for (EntityPlayer player : mob.world.playerEntities)
        {
            player.sendMessage(new TextComponentTranslation(str));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void noticeDespawn(AllowDespawn event)
    {
        //■コンフィグ
        if (Configs.getIsNoticeDespawnMessage() == false) { return; }

        //■DENYならデスポンしないので通知不要
        if (event.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) { return; }

        if (event.getEntityLiving() != null && event.getEntityLiving().world.isRemote == true) { return; }

        //■村人護り隊 が攻撃された！
        if (event.getEntityLiving() instanceof EntityCreature == false) { return; }
        EntityCreature mob = (EntityCreature)event.getEntityLiving();
        if (Utils.isGuardian(mob) == false) { return; }

        for (EntityPlayer player : mob.world.playerEntities)
        {
            player.sendMessage(new TextComponentTranslation("Despawn : " + mob.getName()));
        }
    }
}
