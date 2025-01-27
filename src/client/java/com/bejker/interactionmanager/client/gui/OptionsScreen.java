package com.bejker.interactionmanager.client.gui;

import com.bejker.interactionmanager.client.config.InteractionManagerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.text.Text;


public class OptionsScreen extends GameOptionsScreen {
    public OptionsScreen(Screen parent) {
        super(parent,MinecraftClient.getInstance().options,Text.translatable("interactionmanager.options"));
    }


    @Override
    protected void addOptions() {
        if(this.body != null){
            this.body.addAll(InteractionManagerConfig.asOptions());
        }
    }

    @Override
    public void removed() {
        InteractionManagerConfig.saveConfig();
    }
}
