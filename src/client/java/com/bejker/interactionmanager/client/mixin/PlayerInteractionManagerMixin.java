package com.bejker.interactionmanager.client.mixin;

import com.bejker.interactionmanager.client.InteractionManagerClient;
import com.bejker.interactionmanager.config.InteractionManagerConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class PlayerInteractionManagerMixin {

    @Inject(method = "interactBlock",at= @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableObject;<init>()V"),cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        InteractionManagerClient.onInteractBlock(player,hand,hitResult,cir);
    }
}
