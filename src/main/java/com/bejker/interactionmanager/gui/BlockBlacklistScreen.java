package com.bejker.interactionmanager.gui;

import com.bejker.interactionmanager.gui.widget.BlockListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Optional;


public class BlockBlacklistScreen extends BlacklistScreen {

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.block_blacklist");

    private BlockListWidget blockList;

    public BlockBlacklistScreen(Screen parent) {
        super(parent, TITLE_TEXT);
    }

    @Override
    protected void initBody() {
        super.initBody();
        blockList = layout.addBody(new BlockListWidget(this, this.client));
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        this.blockList.position(this.width, this.layout);
    }

}
