package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.Interactions;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "isBlockBreakingRestricted",at = @At("RETURN"),cancellable = true)
    void restrictBlockBreaking(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()){
            return;
        }
        Block block = world.getBlockState(pos).getBlock();
        Interactions.restrictBlockBreaking(block,cir);
    }
}
