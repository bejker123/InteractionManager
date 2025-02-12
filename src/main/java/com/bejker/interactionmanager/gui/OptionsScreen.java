package com.bejker.interactionmanager.gui;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SimpleOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsScreen extends SimpleOptionsScreen {
    private ButtonWidget restore_defaults;
    private ButtonWidget block_blacklist;
    private ButtonWidget entity_blacklist;

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.interactions");
    public OptionsScreen(Screen parent) {
        super(parent,MinecraftClient.getInstance().options,TITLE_TEXT,Config.asOptions());
        ConfigManager.loadConfig();
    }

    @Override
    public void removed() {
        ConfigManager.saveConfig();
    }

    protected void init() {
        //List<ClickableWidget> option_widgets = Arrays.stream(Config.asOptions()).map((x) -> x.createWidget(gameOptions,0,0,150)).toList();
        //ArrayList<ClickableWidget> widgets = new ArrayList<>(option_widgets);
        //widgets.forEach(this::addDrawableChild);
        this.buttonList = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        this.buttonList.addAll(this.options);
        this.addSelectableChild(this.buttonList);
        this.block_blacklist = this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.interactionmanager.block_blacklist"),(button) -> {
            client.setScreen(new BlockBlacklistScreen(this));
        }).build());

        this.restore_defaults = this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.interactionmanager.restore_defaults"), button -> {
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
        }).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build());
        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> this.client.setScreen(this.parent))
                        .dimensions(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
                        .build()
        );
        //DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        //directionalLayoutWidget.add(this.restore_defaults);
        //directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        this.restore_defaults.active = !ConfigManager.areOptionValuesSetToDefault();
        if(this.restore_defaults.active){
           this.restore_defaults.setTooltip(Tooltip.of(Text.translatable("button.interactionmanager.restore_defaults.active.tooltip")));
        }else{
            this.restore_defaults.setTooltip(null);
        }

      //this.block_blacklist.active = Config.ENABLE_BLOCK_BLACKLIST.getValue();

      //this.entity_blacklist.active = Config.ENABLE_ENTITY_BLACKLIST.getValue();

        super.render(context,mouseX,mouseY,delta);
    }
}
