package com.bejker.interactionmanager.gui.options.denylist;

import com.bejker.interactionmanager.gui.widget.BlockListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class BlockDenylistScreen extends DenyListScreen {

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.block_deny_list");

    private BlockListWidget blockList;

    public BlockDenylistScreen(Screen parent) {
        super(parent, TITLE_TEXT);
    }

    @Override
    protected void initBody() {
        super.initBody();
        blockList = layout.addBody(new BlockListWidget(this, this.client));
        this.refreshWidgetPositions();
    }

    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        this.blockList.position(this.width, this.layout);
    }

}
