package com.bejker.interactionmanager.client.gui;

import com.bejker.interactionmanager.client.config.Config;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class BlockBlacklistScreen extends Screen {

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.block_blacklist");

    private final Screen parent;

    ClickableWidget allow_breaking_blocks;

    public BlockBlacklistScreen(Screen parent) {
        super(TITLE_TEXT);
        this.parent = parent;
    }

    @Override
    protected void init() {
        allow_breaking_blocks = Config.ALLOW_BREAKING_BLOCKS.asOption().createWidget(MinecraftClient.getInstance().options);
        this.addDrawableChild(allow_breaking_blocks);
    }

    @Override
    public void close() {
        if(allow_breaking_blocks instanceof SimpleOption.OptionSliderWidgetImpl<?> optionSliderWidget){
            optionSliderWidget.applyPendingValue();
        }
        client.setScreen(parent);
    }
}
