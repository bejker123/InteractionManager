package com.bejker.interactionmanager.gui.options.blacklist;

import com.bejker.interactionmanager.gui.widget.EntityListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class EntityDenyListScreen extends DenyListScreen {

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.entity_deny_list");

    private EntityListWidget entityList;

    public EntityDenyListScreen(Screen parent) {
        super(parent, TITLE_TEXT);
    }

    @Override
    protected void initBody() {
        super.initBody();
        entityList = layout.addBody(new EntityListWidget(this, this.client));
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        this.entityList.position(this.width, this.layout);
    }

}
