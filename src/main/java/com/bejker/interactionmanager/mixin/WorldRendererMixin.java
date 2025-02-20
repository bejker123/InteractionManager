package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.Interactions;
import com.bejker.interactionmanager.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private ClientWorld world;

    @Inject(method = "drawBlockOutline",at = @At("HEAD"),cancellable = true)
    void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo ci){
        if(!Config.RENDER_PROTECTED_BLOCKS.getValue()){
            return;
        }
        CallbackInfoReturnable<Boolean> cir = new CallbackInfoReturnable<>("restrict",true);
        cir.setReturnValue(false);
        Interactions.restrictBlockBreaking(state.getBlock(),cir);
        if(!cir.getReturnValue()){
            return;
        }
        ci.cancel();
        drawCuboidShapeOutline(
                matrices,
                vertexConsumer,
                state.getOutlineShape(this.world, pos, ShapeContext.of(entity)),
                (double)pos.getX() - cameraX,
                (double)pos.getY() - cameraY,
                (double)pos.getZ() - cameraZ,
                0.85F,
                0.3F,
                0.3F,
                0.4F
        );
    }

    @Shadow
    private static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape outlineShape, double v, double v1, double v2, float r, float g, float b, float a) {
    }
}
