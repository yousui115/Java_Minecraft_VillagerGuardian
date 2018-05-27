package yousui115.villagerguardian.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerHeadWaterLily implements LayerRenderer<EntityEnderman>
{
    //■頭
    private final ModelRenderer modelHead;

    //■胴体
    private final ModelRenderer modelBody;


    /**
     * ■コンストラクタ
     * @param headIn
     */
    public LayerHeadWaterLily(ModelRenderer headIn, ModelRenderer bodyIn)
    {
        modelHead = headIn;

        modelBody = bodyIn;
    }

    /**
     * ■どうしてエンダーマンの頭上のスイレンの葉が滑り落ちないのはなぜ？
     */
    @Override
    public void doRenderLayer(EntityEnderman endermanIn, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        //■頭のアイテムを取得
        ItemStack head = endermanIn.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        //■頭（スイレンの葉 以外はお帰り下さい）
        if (head.getItem() instanceof ItemBlock == false || ((ItemBlock)head.getItem()).getBlock() != Blocks.WATERLILY) { return; }


        //■昼 or 雨
        if (endermanIn.world.getWorldTime() % 24000 < 12000 || endermanIn.world.isRaining() == true)
        {
            //■座標の保存
            GlStateManager.pushMatrix();

            //■頭の動きに同期させる
            this.modelHead.postRender(0.0625F);

            //■移動
            GlStateManager.translate(0.15f, 0.5f, -2.2f);

            //■拡縮
            GlStateManager.scale(2.5d, 2.5d, 2.5d);

            //■回転
            GlStateManager.rotate(90.0f, 1f, 0f, 0f);

            //■色
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            //■描画
            Minecraft.getMinecraft().getItemRenderer().renderItem(endermanIn, head, ItemCameraTransforms.TransformType.HEAD);

            //■元の座標を読み込む
            GlStateManager.popMatrix();

        }
        else
        {
            //■座標の保存
            GlStateManager.pushMatrix();

            //■胴の動きに同期させる
            this.modelBody.postRender(0.0625F);

//            //■移動
            GlStateManager.translate(0.0f, 0.0f, 0.25f);
//
//            //■拡縮
            GlStateManager.scale(2.5d, 2.5d, 2.5d);
//
//            //■回転
//            GlStateManager.rotate(90.0f, 1f, 0f, 0f);
//
//            //■色
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            //■描画
            Minecraft.getMinecraft().getItemRenderer().renderItem(endermanIn, head, ItemCameraTransforms.TransformType.FIXED);

            //■元の座標を読み込む
            GlStateManager.popMatrix();

        }
    }

    @Override
    public boolean shouldCombineTextures() { return false; }

}
