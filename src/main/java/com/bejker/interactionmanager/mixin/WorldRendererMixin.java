package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.Interactions;
import com.bejker.interactionmanager.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
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
    void onDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, int color, CallbackInfo ci){
        if(!Config.RENDER_PROTECTED_BLOCKS.getValue()){
            return;
        }
        CallbackInfoReturnable<Boolean> cir = new CallbackInfoReturnable<>("restrict",true);
        cir.setReturnValue(false);
        Interactions.restrictBlockBreaking(state.getBlock(),cir);
        if(cir.getReturnValue()){
            ci.cancel();
            VertexRendering.drawOutline(
                    matrices,
                    vertexConsumer,
                    state.getOutlineShape(this.world, pos, ShapeContext.of(entity)),
                    (double)pos.getX() - cameraX,
                    (double)pos.getY() - cameraY,
                    (double)pos.getZ() - cameraZ,
                    ColorHelper.fromFloats(
                    0.85F,
                    0.3F,
                    0.3F,
                    0.4F)
            );
        }
        if(Config.RENDER_DENIED_BLOCK_PLACEMENT.getValue()&&MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr){
            BlockPos newBlockPos = bhr.getBlockPos().offset(bhr.getSide(),1);
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player == null){
                return;
            }
            for (Hand hand : Hand.values()){
                ItemStack stack = player.getStackInHand(hand);
                if(stack.getItem() instanceof BlockItem blockItem){
                    BlockState newState = blockItem.getBlock().getPlacementState(ItemPlacementContext.offset(new ItemPlacementContext(player,hand,stack,bhr),newBlockPos,bhr.getSide()));
                    if(newState == null){
                        continue;
                    }
                    CallbackInfoReturnable<ActionResult> actionResult = new CallbackInfoReturnable<>("allowAction",true);
                    Interactions.onInteractBlock(stack,state.getBlock(),actionResult);
                    if(actionResult.getReturnValue() == ActionResult.PASS){
                        VertexRendering.drawOutline(
                                matrices,
                                vertexConsumer,
                                newState.getOutlineShape(this.world, newBlockPos),
                                newBlockPos.getX() - cameraX,
                                newBlockPos.getY() - cameraY,
                                newBlockPos.getZ() - cameraZ,
                                ColorHelper.fromFloats(
                                0.9F,
                                0.51F,
                                0.2F,
                                0.5F)
                        );
                    }
                    break;
                }

            }
        }
    }
}
