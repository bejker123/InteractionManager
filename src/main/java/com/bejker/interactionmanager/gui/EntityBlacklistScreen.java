package com.bejker.interactionmanager.gui;

import com.bejker.interactionmanager.gui.widget.EntityListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class EntityBlacklistScreen extends BlacklistScreen {

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.entity_blacklist");

    private EntityListWidget entityList;

    public EntityBlacklistScreen(Screen parent) {
        super(parent, TITLE_TEXT);
    }

    @Override
    protected void initBody() {
        super.initBody();
        //entityList = layout.addBody(new EntityListWidget(this, this.client));
        entityList = new EntityListWidget(this,this.client);
        this.addDrawableChild(entityList);
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        //this.entityList.position(this.width, this.layout);
    }

}
