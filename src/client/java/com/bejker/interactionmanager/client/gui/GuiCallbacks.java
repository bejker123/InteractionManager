package com.bejker.interactionmanager.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;

public class GuiCallbacks {
    public static void optionsScreenOnInitWidgets(GridWidget.Adder adder){
        MinecraftClient client = MinecraftClient.getInstance();
        adder.add(
                ButtonWidget.builder(Text.literal("Interactions"),
                (interactions_button) -> {
                    client.setScreen(new OptionsScreen(Text.of("Interactions"), client.currentScreen));
                })
                .build());
    }
}

