package yousui115.villagerguardian.util;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yousui115.villagerguardian.VillagerGuardian;

public class VGItems
{
    //■アイテム
    public static Item UMBRELLA;

    /**
     * ■生成
     */
    public static void create()
    {
        //■傘
        UMBRELLA = new Item()
                    {
                        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
                        {
                            if (tab == CreativeTabs.MISC)
                            {
                                subItems.add(new ItemStack(this, 1, 0));
                                subItems.add(new ItemStack(this, 1, 1));
                            }
                        }
                    }
                  .setRegistryName(new ResourceLocation(VillagerGuardian.MOD_ID, "umbrella"))
                  .setUnlocalizedName("umbrella")
                  .setMaxStackSize(1)
                  .setHasSubtypes(true)
                  .setCreativeTab(CreativeTabs.MISC);
    }

    /**
     * ■登録
     * @param event
     */
    public static void register(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(UMBRELLA);
    }


    @SideOnly(Side.CLIENT)
    public static void registerModel()
    {
        ResourceLocation rl1 = new ResourceLocation(VillagerGuardian.MOD_ID, "umbrella_open");
        ResourceLocation rl2 = new ResourceLocation(VillagerGuardian.MOD_ID, "umbrella_close");

        ModelBakery.registerItemVariants(UMBRELLA, rl1, rl2);

        ModelLoader.setCustomModelResourceLocation(UMBRELLA, 0, new ModelResourceLocation(rl1, "inventory"));
        ModelLoader.setCustomModelResourceLocation(UMBRELLA, 1, new ModelResourceLocation(rl2, "inventory"));

    }
}
