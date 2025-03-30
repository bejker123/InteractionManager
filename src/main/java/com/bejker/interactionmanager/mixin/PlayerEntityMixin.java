package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.Interactions;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin{
    @Inject(method = "isBlockBreakingRestricted",at = @At("RETURN"),cancellable = true)
    void restrictBlockBreaking(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()){
            return;
        }
        Block block = world.getBlockState(pos).getBlock();
        Interactions.restrictBlockBreaking(block,cir);
    }
    @Inject(method = "tick",at = @At("RETURN"))
    void onTick(CallbackInfo ci){
        if((PlayerEntity)(Object)this instanceof ClientPlayerEntity player) {
            Interactions.onTick(player, ci);
        }
    }
}
