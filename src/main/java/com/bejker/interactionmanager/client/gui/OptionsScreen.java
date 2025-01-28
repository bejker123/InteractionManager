package com.bejker.interactionmanager.client.gui;

import com.bejker.interactionmanager.client.config.InteractionManagerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class OptionsScreen extends GameOptionsScreen {
    private ButtonWidget restore_defaults;

    public OptionsScreen(Screen parent) {
        super(parent,MinecraftClient.getInstance().options,Text.translatable("screen.interactionmanager.interactions"));
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

    @Override
    protected void initFooter() {
        this.restore_defaults = ButtonWidget.builder(Text.translatable("button.interactionmanager.restore_defaults"), button -> {
            InteractionManagerConfig.restoreDefaults();
            this.close();
            client.setScreen(new OptionsScreen(client.currentScreen));
        }).build();
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        directionalLayoutWidget.add(this.restore_defaults);
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        this.restore_defaults.active = !InteractionManagerConfig.areOptionValuesSetToDefault();
        if(this.restore_defaults.active){
           this.restore_defaults.setTooltip(Tooltip.of(Text.translatable("button.interactionmanager.restore_defaults.active.tooltip")));
        }else{
            this.restore_defaults.setTooltip(null);
        }

        for (Drawable drawable : this.drawables) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }
}
