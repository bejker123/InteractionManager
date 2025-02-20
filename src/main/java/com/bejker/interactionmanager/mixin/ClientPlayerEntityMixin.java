package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.config.Config;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "dropSelectedItem",at = @At("HEAD"),cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if(!Config.ALLOW_DROPPING_ITEMS.getValue()){
            cir.setReturnValue(false);
            return;
        }
        ClientPlayerEntity player = (ClientPlayerEntity) ((Object) this);
        if(Config.LOCK_HOT_BAR.getValue()&&0 <= player.getInventory().selectedSlot&& player.getInventory().selectedSlot <= 9){
            cir.setReturnValue(false);
            return;
        }

    }
}
