package com.bejker.interactionmanager.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;

public class GuiCallbacks {
    public static void optionsScreenOnInitWidgets(Screen screen, GridWidget gridWidget){
        //for (int i = 0; i < gridWidget.children.size(); i++) {
        //   ButtonWidget button = (ButtonWidget) gridWidget.children.get(i);
        //   CLIENT_LOGGER.info("{}",button.getMessage());
        //}
        MinecraftClient client = MinecraftClient.getInstance();
        gridWidget.add(
                ButtonWidget.builder(Text.literal("Interactions"),
                (interactions_button) -> {
                    client.setScreen(new OptionsScreen(Text.of("Interactions"), client.currentScreen));
                })
                .build()
                ,6,0);
    }
}

