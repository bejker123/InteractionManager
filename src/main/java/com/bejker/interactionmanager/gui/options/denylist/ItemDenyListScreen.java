package com.bejker.interactionmanager.gui.options.denylist;

import com.bejker.interactionmanager.gui.widget.EntityListWidget;
import com.bejker.interactionmanager.gui.widget.ItemDenyListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class ItemDenyListScreen extends DenyListScreen {

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.item_deny_list");

    private ItemDenyListWidget entityList;

    public ItemDenyListScreen(Screen parent) {
        super(parent, TITLE_TEXT);
    }

    @Override
    protected void initBody() {
        super.initBody();
        entityList = layout.addBody(new ItemDenyListWidget(this, this.client));
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        this.entityList.position(this.width, this.layout);
        search.setX(this.width / 2 - search.getWidth() / 2);
    }

}
