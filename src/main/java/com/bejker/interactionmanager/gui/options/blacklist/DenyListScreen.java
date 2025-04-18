package com.bejker.interactionmanager.gui.options.blacklist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public abstract class DenyListScreen extends GameOptionsScreen {

    private final Screen parent;

    protected TextWidget titleWidget;
    TextFieldWidget search;
    protected boolean focusSearchOnKeyPress = true;

    public DenyListScreen(Screen parent, Text title) {
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
        super.initBody();
        //blockList = layout.addBody(new BlockListWidget(this, this.client));
    }

    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        //this.listWidget.position(this.width, this.layout);
        titleWidget.setX(this.width / 2 - titleWidget.getWidth() / 2);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //If ESC is pressed we exit early
        if(super.keyPressed(keyCode, scanCode, modifiers)){
            return true;
        }

        if(focusSearchOnKeyPress){
            //If no modifiers(except num lock, caps lock and shift) are held we assume the user wants to search,
            //and focus the search widget.
            //Modifiers are set using individual bits, so when no modifiers are present modifiers == 0
            int not_ignored_mods = modifiers & ~GLFW.GLFW_MOD_NUM_LOCK & ~GLFW.GLFW_MOD_CAPS_LOCK & ~GLFW.GLFW_MOD_SHIFT;
            if(not_ignored_mods == 0){
                search.setFocused(true);
                this.setFocused(search);
            }
        }
        return false;
    }
}
