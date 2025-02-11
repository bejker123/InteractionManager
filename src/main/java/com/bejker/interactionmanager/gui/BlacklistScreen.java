package com.bejker.interactionmanager.gui;

import com.bejker.interactionmanager.gui.widget.BlockListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

import java.util.Locale;

public abstract class BlacklistScreen extends GameOptionsScreen {

    private final Screen parent;

    private TextWidget titleWidget;
    TextFieldWidget search;

    public BlacklistScreen(Screen parent,Text title) {
        super(parent, MinecraftClient.getInstance().options, title);
        this.parent = parent;
    }

    @Override
    protected void initHeader() {
        DirectionalLayoutWidget widget = new DirectionalLayoutWidget(0,0, DirectionalLayoutWidget.DisplayAxis.VERTICAL);
        widget.getMainPositioner().margin(0,5);
        widget.getMainPositioner().alignVerticalCenter();
        widget.setY(0);
        titleWidget = new TextWidget(title,textRenderer);
        titleWidget.alignCenter();
        widget.add(titleWidget);
        search = new TextFieldWidget(textRenderer,150,20,Text.empty());
        //search.setX(width / 2 - search.getWidth() / 2);
        widget.add(search);
        this.layout.addHeader(widget);
        this.layout.setHeaderHeight(this.layout.getHeaderHeight() + 20);
    }

    @Override
    protected void initBody() {
        this.addDrawableChild(search);
        //blockList = layout.addBody(new BlockListWidget(this, this.client));
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        //this.listWidget.position(this.width, this.layout);
        titleWidget.setX(titleWidget.getX() + titleWidget.getWidth() / 2);
    }

    @Override
    protected void init() {
        super.init();
        this.refreshWidgetPositions();
    }

    @Override
    protected void addOptions() {
    }

    @Override
    public void close() {
        if(client == null){
            return;
        }
        client.setScreen(parent);
    }

    public String getSearch() {
        return this.search.getText().strip().toLowerCase(Locale.ROOT);
    }
}
