package yousui115.villagerguardian;

import org.apache.logging.log4j.Logger;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import yousui115.villagerguardian.event.EventCreature;
import yousui115.villagerguardian.event.EventEnderman;
import yousui115.villagerguardian.event.EventVillager;
import yousui115.villagerguardian.util.Configs;
import yousui115.villagerguardian.util.VGItems;

@Mod(modid = VillagerGuardian.MOD_ID, name = VillagerGuardian.MOD_NAME, version = VillagerGuardian.VERSION, useMetadata = true)
@EventBusSubscriber
public class VillagerGuardian
{
    public static final String MOD_ID = "villagerguardian";
    public static final String MOD_DOMAIN = "yousui115." + MOD_ID;

    public static final String MOD_NAME = "Villager Guardian";
    public static final String VERSION  = "M1122_F2611_v7";

    //■インスタント
    @Mod.Instance(MOD_ID)
    public static VillagerGuardian INSTANCE;

    //■ぷろくし
    @SidedProxy(clientSide = MOD_DOMAIN + ".client.ClientProxy", serverSide = MOD_DOMAIN + ".CommonProxy")
    public static CommonProxy proxy;

    //■ロガー
    public static Logger logger;


    /**
     * ■
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        //■コンフィグ
        Configs.setting(event);

    }

    /**
     * ■
     */
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        //■HMaGを導入しているか否か
        for (ModContainer cont : Loader.instance().getModList())
        {
            //TODO 複数判定する場合があるので、おいそれとbreakさせない
            if (cont.getModId().equals("hmag")) { Configs.isInstHMaG = true; break;}
        }

        //■
        MinecraftForge.EVENT_BUS.register(new EventCreature());
        MinecraftForge.EVENT_BUS.register(new EventVillager());
        MinecraftForge.EVENT_BUS.register(new EventEnderman());

        //■
        proxy.addLayer();

    }

    /**
     * ■アイテムの登録
     * @param event
     */
    @SubscribeEvent
    protected static void registerItem(RegistryEvent.Register<Item> event)
    {
        //■アイテムの生成と登録
        VGItems.create();
        VGItems.register(event);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        proxy.registerModel();
    }
}
