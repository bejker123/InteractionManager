package com.bejker.interactionmanager.client.mixin;

import com.bejker.interactionmanager.client.InteractionManagerClient;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registerable;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.ibm.icu.impl.ValidIdentifiers.Datatype.u;

@Mixin(ClientPlayerInteractionManager.class)
public class PlayerInteractionManagerMixin {

    @Inject(method = "interactBlock",at= @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableObject;<init>()V"),cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(player == null || MinecraftClient.getInstance().world == null){
            return;
        }
        ItemStack stack = player.getStackInHand(hand);
        Block block = MinecraftClient.getInstance().world.getBlockState(hitResult.getBlockPos()).getBlock();

        InteractionManagerClient.onInteractBlock(stack,block,cir);
    }
    @Inject(method = "attackEntity",at=@At("HEAD"),cancellable = true)
    public void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        InteractionManagerClient.onAttackEntity(player.getUuid(),target,ci);
    }
}
