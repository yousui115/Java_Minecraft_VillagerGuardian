package yousui115.villagerguardian;

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
            isNoticeDamageMessage  = cfg.getBoolean("damage", "notice (Debug)", false, "display a damage message");
            isNoticeDeathMessage   = cfg.getBoolean("death", "notice (Debug)", false, "display a death message");
            isNoticeDespawnMessage = cfg.getBoolean("despawn", "notice (Debug)", false, "display a despawn message");
        }
        finally
        {
            cfg.save();
        }

    }
}
