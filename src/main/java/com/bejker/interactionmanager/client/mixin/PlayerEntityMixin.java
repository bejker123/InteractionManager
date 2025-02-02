package com.bejker.interactionmanager.client.mixin;

import com.bejker.interactionmanager.client.InteractionManagerClient;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract boolean canInteractWithBlockAt(BlockPos pos, double additionalRange);

    @Inject(method = "isBlockBreakingRestricted",at = @At("RETURN"),cancellable = true)
    void restrictBlockBreaking(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()){
            return;
        }
        Block block = world.getBlockState(pos).getBlock();
        InteractionManagerClient.restrictBlockBreaking(block,cir);
    }
}
