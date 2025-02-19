package com.bejker.interactionmanager;

import com.bejker.interactionmanager.config.Config;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.UUID;

public class RenderProtected {
    private static final HashMap<UUID,Long> recentlyProtectedEntities = new HashMap<>();
    private static final HashMap<UUID,Long> recentlyUnprotectedEntities = new HashMap<>();

    public static final long RENDER_PROTECTED_TIME = 1000; // Milliseconds
    public static final float RENDER_PROTECTED_CUTOFF = 1000.f; // Milliseconds

    public static void markAsProtected(Entity entity){
        recentlyProtectedEntities.put(entity.getUuid(), Util.getMeasuringTimeMs() + RENDER_PROTECTED_TIME);
    }

    public static void markAsUnprotected(Entity entity){
        recentlyUnprotectedEntities.put(entity.getUuid(), Util.getMeasuringTimeMs() + RENDER_PROTECTED_TIME);
    }

    // public static final RenderPhase.Transparency TRANSPARENCY = new RenderPhase.Transparency(
   //         "translucent_transparency",
   //         () -> {
   //             RenderSystem.enableBlend();
   //             RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE_MINUS_SRC_ALPHA,
   //                 GlStateManager.DstFactor.SRC_ALPHA);
   //         },
   //         () -> {
   //             RenderSystem.disableBlend();
   //             RenderSystem.defaultBlendFunc();
   //         }
   // );

   // private static final RenderLayer.MultiPhase LAYER = RenderLayer.of(
   //         "debug_filled_box",
   //         VertexFormats.POSITION_COLOR,
   //         VertexFormat.DrawMode.TRIANGLE_STRIP,
   //         1536,
   //         true,
   //         false,
   //         RenderLayer.MultiPhaseParameters.builder()
   //             .program(RenderLayer.COLOR_PROGRAM)
   //             .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
   //             .transparency(TRANSPARENCY)
   //             .build(true)
   // );

    public static <T extends Entity> void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if(!Config.RENDER_PROTECTED_ENTITIES.getValue()){
            return;
        }

        Long protectedUntil = recentlyProtectedEntities.get(entity.getUuid());
        if (protectedUntil != null) {
            long delta = protectedUntil - Util.getMeasuringTimeMs();
            if (delta <= 0) {
                recentlyProtectedEntities.remove(entity.getUuid());
                return;
            }
            calcAndDrawBox(entity,matrices,vertexConsumers,0.3f, 0.85f, 0.3f,delta);
        }else{
            Long unprotectedUntil = recentlyUnprotectedEntities.get(entity.getUuid());
            if(unprotectedUntil == null){
                return;
            }
            long delta = unprotectedUntil - Util.getMeasuringTimeMs();
            if (delta <= 0) {
                recentlyUnprotectedEntities.remove(entity.getUuid());
                return;
            }
            calcAndDrawBox(entity,matrices,vertexConsumers,0.85f, 0.3f, 0.3f,delta);
        }
    }

   private static <T extends Entity> void calcAndDrawBox(T entity,MatrixStack matrices,VertexConsumerProvider vertexConsumers,float r,float g,float b,long delta){
       float new_delta = Math.min(delta / RENDER_PROTECTED_CUTOFF, 1.0f);
       float alpha = MathHelper.lerp(easeInQuad(new_delta), 0.0f, 0.71f);
       drawBox(entity,matrices,vertexConsumers,r, g, b, alpha);
   }

    private static <T extends Entity> void drawBox(T entity,
                                                  MatrixStack matrices,
                                                  VertexConsumerProvider vertexConsumers,
                                                  float r,
                                                  float g,
                                                  float b,
                                                  float a) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        Box box = entity.getBoundingBox()
                .offset(-entity.getX(), -entity.getY(), -entity.getZ());
        VertexRendering.drawBox(matrices,
                consumer,
                box.minX,
                box.minY,
                box.minZ,
                box.maxX,
                box.maxY,
                box.maxZ ,
                r,
                g,
                b,
                a);
    }

    private static float easeInQuad(float x) {
        return (float) Math.pow(x,2);
    }

}
