package yousui115.villagerguardian.util;

import hmag.client.render.RenderEnderExecutor;
import hmag.entity.EntityEnderExecutor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import yousui115.villagerguardian.client.LayerUmbrella;

public class Utils_HMaG
{
    /**
     * ■
     */
    public static void addLayer(RenderManager mgr)
    {
        if (mgr.entityRenderMap.containsKey(EntityEnderExecutor.class) == true)
        {
            Render render = mgr.entityRenderMap.get(EntityEnderExecutor.class);

            if (render instanceof RenderEnderExecutor)
            {
                RenderEnderExecutor renderEnderExecutor = (RenderEnderExecutor)render;

                renderEnderExecutor.addLayer(new LayerUmbrella(renderEnderExecutor.getMainModel().bipedHead,
                                                               renderEnderExecutor.getMainModel().bipedBody));
            }
        }
    }
}
