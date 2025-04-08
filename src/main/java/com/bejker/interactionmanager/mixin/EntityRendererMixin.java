package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.RenderProtected;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererMixin<E extends Entity, S extends EntityRenderState> {
    @Inject(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/uti l/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/client/render/entity/state/EntityRenderState;DDDLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V",shift = At.Shift.AFTER))
    private void onRender(E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityRenderer<? super E, S> renderer, CallbackInfo ci, @Local S entityRenderState){
        Vec3d vec3d = renderer.getPositionOffset(entityRenderState);
        double d = x + vec3d.getX();
        double e = y + vec3d.getY();
        double f = z + vec3d.getZ();
        matrices.push();
        matrices.translate(d, e, f);
        RenderProtected.onRender(entity,matrices,vertexConsumers);
        matrices.pop();
    }
}
