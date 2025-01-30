package com.bejker.interactionmanager.client.gui;

import com.bejker.interactionmanager.client.config.InteractionManagerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import static com.bejker.interactionmanager.client.InteractionManagerClient.CLIENT_LOGGER;

public class OptionsScreen extends GameOptionsScreen {
    private ButtonWidget restore_defaults;

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.interactions");
    public OptionsScreen(Screen parent) {
        super(parent,MinecraftClient.getInstance().options,TITLE_TEXT);
        InteractionManagerConfig.loadConfig();
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
            client.setScreen(new ConfirmScreen((restore)->{
                if(restore){
                    InteractionManagerConfig.restoreDefaults();
                }
                //This trickery is done to reload button texts.
                client.setScreen(this);
                this.close();
                client.setScreen(new OptionsScreen(client.currentScreen));
            }, Text.translatable("screen.interactionmanager.restore_defaults"),
                    Text.translatable("confirm.interactionmanager.restore_defaults")));
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
