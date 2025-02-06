package com.bejker.interactionmanager.gui;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsScreen extends GameOptionsScreen {
    private ButtonWidget restore_defaults;
    private ButtonWidget block_blacklist;
    private ButtonWidget entity_blacklist;

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.interactions");
    public OptionsScreen(Screen parent) {
        super(parent,MinecraftClient.getInstance().options,TITLE_TEXT);
        ConfigManager.loadConfig();
    }

    @Override
    protected void addOptions() {
        if(this.body != null){
            List<ClickableWidget> option_widgets = Arrays.stream(Config.asOptions()).map((x) -> x.createWidget(gameOptions)).toList();
            ArrayList<ClickableWidget> widgets = new ArrayList<>(option_widgets);

            block_blacklist = ButtonWidget.builder(Text.translatable("button.interactionmanager.block_blacklist"),(button)->{
                client.setScreen(new BlockBlacklistScreen(this));
            })
              .tooltip(Tooltip.of(Text.translatable("button.interactionmanager.block_blacklist.tooltip")))
              .build();
            widgets.add(block_blacklist);

            // entity_blacklist = ButtonWidget.builder(Text.translatable("button.interactionmanager.entity_blacklist"),)
            // .tooltip(Tooltip.of(Text.translatable("button.interactionmanager.entity_blacklist.tooltip")))
            // .build();

            entity_blacklist = new Button((button)->{
                client.setScreen(new EntityBlacklistScreen(this));
            });

            widgets.add(entity_blacklist);

            this.body.addAll(widgets);
        }
    }

    @Override
    public void removed() {
        ConfigManager.saveConfig();
    }

    @Override
    protected void initFooter() {
        this.restore_defaults = ButtonWidget.builder(Text.translatable("button.interactionmanager.restore_defaults"), button -> {
            if(client == null){
                return;
            }
            client.setScreen(new ConfirmScreen((restore)->{
                if(restore){
                    ConfigManager.restoreDefaults();
                }
                //This trickery is done to reload button texts.
                client.setScreen(this);
                if(restore){
                    this.close();
                    client.setScreen(new OptionsScreen(client.currentScreen));
                }
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

        this.restore_defaults.active = !ConfigManager.areOptionValuesSetToDefault();
        if(this.restore_defaults.active){
           this.restore_defaults.setTooltip(Tooltip.of(Text.translatable("button.interactionmanager.restore_defaults.active.tooltip")));
        }else{
            this.restore_defaults.setTooltip(null);
        }

        this.block_blacklist.active = Config.ENABLE_BLOCK_BLACKLIST.getValue();

        this.entity_blacklist.active = Config.ENABLE_ENTITY_BLACKLIST.getValue();

        for (Drawable drawable : this.drawables) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }
}
