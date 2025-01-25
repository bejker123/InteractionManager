package com.bejker.interactionmanager.client.mixin;

import com.bejker.interactionmanager.client.gui.GuiCallbacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(GameMenuScreen.class)
public abstract class GameMenuMixin extends Screen {
    protected GameMenuMixin(Text title){
        super(title);
    }

    @Inject(method = "initWidgets",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;forEachChild(Ljava/util/function/Consumer;)V"),locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onInitWidgets(CallbackInfo ci, GridWidget gridWidget){
        if(gridWidget == null){
            return;
        }
        GuiCallbacks.gameMenuOnInitWidgets(this,gridWidget);
    }
}
