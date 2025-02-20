package com.bejker.interactionmanager.gui.options;

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
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

public class OptionsScreen extends GameOptionsScreen {
    ButtonWidget restore_defaults;
    private ButtonWidget entity_options;
    private ButtonWidget block_options;

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.interactions");
    protected Class<? extends Annotation> options_target = null;

    public OptionsScreen(Screen parent) {
        this(parent,TITLE_TEXT);
    }

    public OptionsScreen(Screen parent,Text title) {
        super(parent,MinecraftClient.getInstance().options,title);
        ConfigManager.loadConfig();
    }

    @Override
    protected void addOptions() {
        if(this.body != null){
            ArrayList<ClickableWidget> widgets = new ArrayList<>();

            entity_options = ButtonWidget.builder(Text.translatable("button.interactionmanager.entity_options"),(button)->{
                        if(client == null){
                            return;
                        }
                        client.setScreen(new EntityOptionsScreen(this));
                    })
                    .tooltip(Tooltip.of(Text.translatable("button.interactionmanager.entity_options.tooltip")))
                    .build();
            widgets.add(entity_options);

            block_options = ButtonWidget.builder(Text.translatable("button.interactionmanager.block_options"),(button)->{
                        if(client == null){
                            return;
                        }
                        client.setScreen(new BlockOptionsScreen(this));
                    })
                    .tooltip(Tooltip.of(Text.translatable("button.interactionmanager.block_options.tooltip")))
                    .build();
            widgets.add(block_options);

            this.body.addAll(widgets);
        }
    }

    @Override
    public void removed() {
        ConfigManager.saveConfig();
    }

    protected void reopen(){
        if(client == null){
            return;
        }
        this.close();
        client.setScreen(new OptionsScreen(client.currentScreen));
    }

    @Override
    protected void initFooter() {
        this.restore_defaults = ButtonWidget.builder(Text.translatable("button.interactionmanager.restore_defaults"), button -> {
            if(client == null){
                return;
            }
            client.setScreen(new ConfirmScreen((restore)->{
                if(restore){
                    ConfigManager.restoreDefaults(this.options_target);
                }
                //This trickery is done to reload button texts.
                client.setScreen(this);
                if(restore){
                    this.reopen();
                }
            }, Text.translatable("screen.interactionmanager.restore_defaults"),
                    Text.translatable("confirm.interactionmanager.restore_defaults")));
        }).build();
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        directionalLayoutWidget.add(this.restore_defaults);
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
    }

    protected boolean isRestoreDefaultsActive(){
        return !ConfigManager.areOptionValuesSetToDefault(this.options_target);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        this.restore_defaults.active = this.isRestoreDefaultsActive();
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
