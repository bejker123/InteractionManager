package com.bejker.interactionmanager.mixin;

import com.bejker.interactionmanager.gui.GuiCallbacks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Text title) {
        super(title);
    }

   @Inject(method = "init",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;forEachChild(Ljava/util/function/Consumer;)V"),locals = LocalCapture.CAPTURE_FAILSOFT)
    protected void onInit(CallbackInfo ci,GridWidget gridWidget,GridWidget.Adder adder) {
        if(gridWidget == null){
            return;
        }
       GuiCallbacks.optionsScreenOnInitWidgets(adder);
    }
}
