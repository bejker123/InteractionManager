package com.bejker.interactionmanager.gui;

import com.bejker.interactionmanager.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;

public class GuiCallbacks {
    public static void optionsScreenOnInitWidgets(GridWidget.Adder adder){
        Config.ShouldAddInteractionsButton shouldAddInteractionsButton = Config.SHOULD_ADD_INTERACTIONS_BUTTON.getValue();
        if(shouldAddInteractionsButton == Config.ShouldAddInteractionsButton.NEVER){
            return;
        }
        if(shouldAddInteractionsButton == Config.ShouldAddInteractionsButton.ONLY_IF_MOD_MENU_IS_NOT_INSTALLED&&
            Config.IS_MODMENU_INSTALLED.getValue()){
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        adder.add(
                ButtonWidget.builder(Text.translatable("screen.interactionmanager.interactions"),
                (interactions_button) -> {
                    client.setScreen(new OptionsScreen(client.currentScreen));
                })
                .build());
    }
}

