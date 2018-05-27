package yousui115.villagerguardian.client;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraftforge.fml.client.FMLClientHandler;
import yousui115.villagerguardian.CommonProxy;

public class ClientProxy extends CommonProxy
{
    /**
     * ■レイヤーの追加
     */
    @Override
    public void addLayer()
    {
        //■エンダーマン
        if (getRenderMgr().entityRenderMap.containsKey(EntityEnderman.class) == true)
        {
            Render render = getRenderMgr().entityRenderMap.get(EntityEnderman.class);

            if (render instanceof RenderEnderman)
            {
                RenderEnderman renderEnderman = (RenderEnderman)render;

                renderEnderman.addLayer(new LayerHeadWaterLily(renderEnderman.getMainModel().bipedHead,
                                                               renderEnderman.getMainModel().bipedBody));
            }
        }
    }

    //■RenderManager
    public static RenderManager getRenderMgr() { return FMLClientHandler.instance().getClient().getRenderManager(); }

}
