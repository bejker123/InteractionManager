package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.Interactions;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
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
    @Inject(method = "interactItem",at = @At("HEAD"),cancellable = true)
    public void onInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(player == null||hand == null){
            return;
        }
        Interactions.onUseItem(player,hand,cir);
    }

    @Inject(method = "interactEntity",at = @At("HEAD"),cancellable = true)
    public void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(player == null||entity == null||hand == null){
            return;
        }
        Interactions.onInteractEntity(player,entity,hand,cir);
    }

    @Inject(method = "interactEntityAtLocation",at = @At("HEAD"),cancellable = true)
    public void interactEntityAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(player == null||entity == null||hand == null){
            return;
        }
        Interactions.onInteractEntity(player,entity,hand,cir);
    }

    @Inject(method = "clickSlot",at = @At("HEAD"),cancellable = true)
    public void onSlotClick(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if(player == null){
            return;
        }
        ScreenHandler screenHandler = player.currentScreenHandler;
        if (syncId != screenHandler.syncId) {
            return;
        }

        Interactions.onSlotClick(syncId,slotId,actionType,player,ci);
    }
}
