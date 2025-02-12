package com.bejker.interactionmanager.gui;

import com.bejker.interactionmanager.gui.widget.BlockListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class BlockBlacklistScreen extends BlacklistScreen {

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.block_blacklist");

    private BlockListWidget blockList;

    public BlockBlacklistScreen(Screen parent) {
        super(parent, TITLE_TEXT);
    }

    @Override
    protected void initBody() {
        super.initBody();
        blockList = new BlockListWidget(this, this.client);
        this.addDrawableChild(blockList);
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        //this.blockList.position(this.width, this.layout);
        this.blockList.updateSize(this.width * 31 / 32,this.height - 43,43,this.height);
        this.blockList.setLeftPos(0);
    }

}
