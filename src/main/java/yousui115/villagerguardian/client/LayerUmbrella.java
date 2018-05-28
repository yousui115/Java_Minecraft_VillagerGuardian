package yousui115.villagerguardian.client;

import hmag.entity.EntityEnderExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import yousui115.villagerguardian.util.VGItems;

@SideOnly(Side.CLIENT)
public class LayerUmbrella implements LayerRenderer<EntityEnderExecutor>
{
    //■頭
    private final ModelRenderer modelHead;

    //■胴体
    private final ModelRenderer modelBody;

    private static ItemStack umbrellaOpen;
    private static ItemStack umbrellaClose;


    /**
     * ■コンストラクタ
     * @param headIn
     */
    public LayerUmbrella(ModelRenderer headIn, ModelRenderer bodyIn)
    {
        modelHead = headIn;

        modelBody = bodyIn;

        if (umbrellaOpen == null || umbrellaClose == null)
        {
            umbrellaOpen = new ItemStack(VGItems.UMBRELLA, 1, 0);
            umbrellaClose = new ItemStack(VGItems.UMBRELLA, 1, 1);
        }
    }

    /**
     * ■
     */
    @Override
    public void doRenderLayer(EntityEnderExecutor enderExeIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        //■頭のアイテムを取得
        ItemStack head = enderExeIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        //■頭（スイレンの葉 以外はお帰り下さい）
        if (head.getItem() instanceof ItemBlock == false || ((ItemBlock)head.getItem()).getBlock() != Blocks.WATERLILY) { return; }


        //■昼 or 雨
        if (enderExeIn.world.getWorldTime() % 24000 < 12000 || enderExeIn.world.isRaining() == true)
        {
            //■座標の保存
            GlStateManager.pushMatrix();

            //■胴の動きに同期させる
            this.modelBody.postRender(0.0625F);

//            //■移動
//            GlStateManager.translate(0.15f, 0.5f, -2.2f);
//
            //■拡縮
            double dSc = 0.75d;
            GlStateManager.scale(dSc, dSc, dSc);

            //■回転
            GlStateManager.rotate(180f, 1f, 0f, 0f);

            GlStateManager.rotate(180f, 0f, 1f, 0f);

            //■色
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            //■描画
            Minecraft.getMinecraft().getItemRenderer().renderItem(enderExeIn, umbrellaOpen, ItemCameraTransforms.TransformType.HEAD);

            //■元の座標を読み込む
            GlStateManager.popMatrix();

        }
        else
        {
            //■座標の保存
            GlStateManager.pushMatrix();

            //■胴の動きに同期させる
            this.modelBody.postRender(0.0625F);

            GlStateManager.translate(0d, 0f, 1d);

            GlStateManager.rotate(30f, 0f, 0f, 1f);

            GlStateManager.rotate(-20f, 1f, 0f, 0f);

            GlStateManager.rotate(180f, 0f, 1f, 0f);

            //■拡縮
            double dSc = 0.75d;
            GlStateManager.scale(dSc, dSc, dSc);

            //■色
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            //■描画
            Minecraft.getMinecraft().getItemRenderer().renderItem(enderExeIn, umbrellaClose, ItemCameraTransforms.TransformType.HEAD);

            //■元の座標を読み込む
            GlStateManager.popMatrix();

        }
    }

    @Override
    public boolean shouldCombineTextures() { return false; }

}
