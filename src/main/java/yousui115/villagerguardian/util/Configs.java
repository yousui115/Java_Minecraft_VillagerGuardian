package yousui115.villagerguardian.util;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Configs
{
    private static boolean isNoticeDamageMessage;
    public static boolean getIsNoticeDamageMessage() { return isNoticeDamageMessage; }

    private static boolean isNoticeDeathMessage;
    public static boolean getIsNoticeDeathMessage() { return isNoticeDeathMessage; }

    private static boolean isNoticeDespawnMessage;
    public static boolean getIsNoticeDespawnMessage() { return isNoticeDespawnMessage; }


    private static String NOTICE_DEBUG = "notice (Debug)";


    //TODO ここがしっくりくる。
    //■コンフィグパラメータ以外
    public static boolean isInstHMaG = false;

    /**
     *
     * @param event
     */
    public static void setting(FMLPreInitializationEvent event)
    {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());

        try
        {
            cfg.load();
            isNoticeDamageMessage  = cfg.getBoolean("damage", NOTICE_DEBUG, false, "display a damage message");
            isNoticeDeathMessage   = cfg.getBoolean("death", NOTICE_DEBUG, false, "display a death message");
            isNoticeDespawnMessage = cfg.getBoolean("despawn", NOTICE_DEBUG, false, "display a despawn message");
        }
        finally
        {
            cfg.save();
        }

    }
}
