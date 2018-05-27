package yousui115.villagerguardian;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import yousui115.villagerguardian.event.EventCreature;
import yousui115.villagerguardian.event.EventEnderman;
import yousui115.villagerguardian.event.EventVillager;

@Mod(modid = VillagerGuardian.MOD_ID, name = VillagerGuardian.MOD_NAME, version = VillagerGuardian.VERSION)
public class VillagerGuardian
{
    public static final String MOD_ID = "villagerguardian";
    public static final String MOD_DOMAIN = "yousui115." + MOD_ID;

    public static final String MOD_NAME = "Villager Guardian";
    public static final String VERSION  = "M1122_F2611_v5";

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
        //■
        MinecraftForge.EVENT_BUS.register(new EventCreature());
        MinecraftForge.EVENT_BUS.register(new EventVillager());
        MinecraftForge.EVENT_BUS.register(new EventEnderman());

        //■
        proxy.addLayer();
    }
}
