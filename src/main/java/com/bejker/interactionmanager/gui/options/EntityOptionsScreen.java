package com.bejker.interactionmanager.gui.options;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.config.ConfigManager;
import com.bejker.interactionmanager.config.option.IEntityOption;
import com.bejker.interactionmanager.gui.options.blacklist.EntityBlacklistScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityOptionsScreen extends OptionsScreen {
    private ButtonWidget entity_blacklist;

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.entity_options");
    public EntityOptionsScreen(Screen parent) {
        super(parent,TITLE_TEXT);
        this.options_target = IEntityOption.class;
    }

    @Override
    protected void addOptions() {
        if(this.body != null){
            List<ClickableWidget> option_widgets = Arrays.stream(Config.asOptions(IEntityOption.class))
                    .map((x) -> x.createWidget(gameOptions)).toList();
            ArrayList<ClickableWidget> widgets = new ArrayList<>(option_widgets);

            entity_blacklist = ButtonWidget.builder(Text.translatable("button.interactionmanager.entity_blacklist"),(button)->{
                if(client == null){
                    return;
                }
                client.setScreen(new EntityBlacklistScreen(this));
            })
            .tooltip(Tooltip.of(Text.translatable("button.interactionmanager.entity_blacklist.tooltip")))
            .build();

            widgets.add(entity_blacklist);

            this.body.addAll(widgets);
        }
    }

    @Override
    protected void reopen(){
        if(client == null){
            return;
        }
        this.close();
        client.setScreen(new EntityOptionsScreen(client.currentScreen));
    }


}
