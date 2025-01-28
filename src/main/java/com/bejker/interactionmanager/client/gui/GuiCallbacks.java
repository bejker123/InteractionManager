package com.bejker.interactionmanager.client.gui;

import com.bejker.interactionmanager.InteractionManager;
import com.bejker.interactionmanager.client.config.InteractionManagerConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;

public class GuiCallbacks {
    public static void optionsScreenOnInitWidgets(GridWidget.Adder adder){
        InteractionManagerConfig.ShouldAddInteractionsButton shouldAddInteractionsButton = InteractionManagerConfig.SHOULD_ADD_INTERACTIONS_BUTTON.getValue();
        if(shouldAddInteractionsButton == InteractionManagerConfig.ShouldAddInteractionsButton.NEVER){
            return;
        }
        if(shouldAddInteractionsButton == InteractionManagerConfig.ShouldAddInteractionsButton.ONLY_IF_MOD_MENU_IS_NOT_INSTALLED&&
            InteractionManagerConfig.IS_MODMENU_INSTALLED.getValue()){
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

