package com.bejker.interactionmanager.client.mixin;

import com.bejker.interactionmanager.client.InteractionManagerClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
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

        InteractionManagerClient.onInteractBlock(stack,block,cir);
    }
    @Inject(method = "attackEntity",at=@At("HEAD"),cancellable = true)
    public void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        InteractionManagerClient.onAttackEntity(player.getUuid(),target,ci);
    }

}
