package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.Interactions;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(method = "interactBlock",at= @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableObject;<init>()V"),cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(player == null || client.world == null){
            return;
        }
        ItemStack stack = player.getStackInHand(hand);
        Block block = client.world.getBlockState(hitResult.getBlockPos()).getBlock();

        Interactions.onInteractBlock(stack,block,cir);
    }
    @Inject(method = "attackEntity",at=@At("HEAD"),cancellable = true)
    public void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        Interactions.onAttackEntity(player.getUuid(),target,ci);
    }

    @Inject(method = "updateBlockBreakingProgress",at = @At("HEAD"),cancellable = true)
    public void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(MinecraftClient.getInstance().world == null){
            return;
        }
        Interactions.restrictBlockBreaking(MinecraftClient.getInstance().world.getBlockState(pos).getBlock(),cir);
    }
}
