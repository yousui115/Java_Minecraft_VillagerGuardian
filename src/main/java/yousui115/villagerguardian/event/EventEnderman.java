package yousui115.villagerguardian.event;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yousui115.villagerguardian.VillagerGuardian;
import yousui115.villagerguardian.util.Utils;

public class EventEnderman
{
    /**
     * ■右クリック！
     * @param event
     */
    @SubscribeEvent
    public void interactMob(PlayerInteractEvent.EntityInteract event)
    {
        //■サーバーのみ
        if (event.getWorld().isRemote) { return; }

        //■対象者
        if (event.getTarget() instanceof EntityEnderman == false) { return; }
        EntityEnderman enderman = (EntityEnderman)event.getTarget();

        //■村人護り隊員のみ
        if (Utils.isGuardian(enderman) == false) { return; }

        //■メインハンドに持つは
        if (event.getHand() != EnumHand.MAIN_HAND) { return; }

        //■スイレンの葉であった。
        if (event.getEntityPlayer().getHeldItemMainhand().getItem() instanceof ItemBlock == false) { return; }
        ItemBlock block = (ItemBlock)event.getEntityPlayer().getHeldItemMainhand().getItem();
        if (block.getBlock() != Blocks.WATERLILY) { return; }

        //■頭装備(装備中の)
        ItemStack head = enderman.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        //■頭にスイレンの葉を、そっとのせてあげる。
        enderman.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(block));

        //■いっこへらす
        event.getEntityPlayer().getHeldItemMainhand().shrink(1);

        if (head != ItemStack.EMPTY)
        {
            enderman.world.spawnEntity(new EntityItem(enderman.world, enderman.posX, enderman.posY, enderman.posZ, head));
        }
    }

    /**
     * ■アップデートイベント
     * @param event
     */
    @SubscribeEvent
    public void cutRainDamage(LivingAttackEvent event)
    {
        //■サーバーのみ
        if (event.getEntityLiving().world.isRemote) { return; }

        //■エンダーマンのみ
        if (event.getEntityLiving() instanceof EntityEnderman == false) { return; }
        EntityEnderman enderman = (EntityEnderman)event.getEntityLiving();

        //■隊員のみ
        if (Utils.isGuardian(enderman) == false) { return; }

        //■雨によるダメージ
        if (event.getSource() == DamageSource.DROWN && enderman.isInWater() == false && Utils.isInRain(enderman) == true)
        {
            //■しかし、スイレンの葉を装備している。
            if (equippedWaterLily(enderman) == true)
            {
                try
                {
                    //isUnblockable = false
                    ObfuscationReflectionHelper.setPrivateValue(DamageSource.class, event.getSource(), false, 20);
                }
                catch(Exception e)
                {
                    //■失敗失敗。てへぺろ
                    VillagerGuardian.logger.log(Level.ERROR, "Reflection Error!");
                    throw e;
                }

                event.setCanceled(true);
            }
        }
    }

    /**
     * ■
     * @param event
     */
    @SubscribeEvent
    public void cancelTeleport(EnderTeleportEvent event)
    {
        //■サーバーのみ
        if (event.getEntityLiving().world.isRemote) { return; }

        //■エンダーマンのみ
        if (event.getEntityLiving() instanceof EntityEnderman == false) { return; }
        EntityEnderman enderman = (EntityEnderman)event.getEntityLiving();

        //■隊員のみ
        if (Utils.isGuardian(enderman) == false) { return; }

        //■スイレンの葉を乗せてる（それ以外は通常の挙動の為）
        if (equippedWaterLily(enderman) == false) { return; }

        //■雨の中
        if (enderman.isInWater() == false && Utils.isInRain(enderman) == true)
        {
        }
    }

    /**
     * ■
     * @param event
     */
    @SubscribeEvent
    public void cancelTeleportDaytime(LivingUpdateEvent event)
    {
        //■サーバーのみ
        if (event.getEntityLiving().world.isRemote) { return; }

        //■エンダーマンのみ
        if (event.getEntityLiving() instanceof EntityEnderman == false) { return; }
        EntityEnderman enderman = (EntityEnderman)event.getEntityLiving();

        //■隊員のみ
        if (Utils.isGuardian(enderman) == false) { return; }

        //■スイレンの葉を乗せている。
        if (equippedWaterLily(enderman) == false) { return; }

        //■リフレクション
        try
        {
            //■日中にランダムワープさせたくない、という強い意志を感じる。
            ObfuscationReflectionHelper.setPrivateValue(EntityEnderman.class, enderman, enderman.ticksExisted, 6);
        }
        catch(Exception e)
        {
            //■失敗失敗。てへぺろ
            VillagerGuardian.logger.log(Level.ERROR, "Reflection Error!");
            throw e;
        }
    }

    //=========================================================================================

    /**
     * ■スイレンの葉を装備しているか否か
     * @param livingIn
     * @return
     */
    private static boolean equippedWaterLily(EntityLiving livingIn)
    {
        ItemStack head = livingIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        if (head != ItemStack.EMPTY &&
            head.getItem() instanceof ItemBlock &&
            ((ItemBlock)head.getItem()).getBlock() == Blocks.WATERLILY)
        {
            return true;
        }

        return false;
    }
}
