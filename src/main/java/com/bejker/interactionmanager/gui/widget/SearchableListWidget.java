package com.bejker.interactionmanager.gui.widget;

import com.bejker.interactionmanager.gui.options.denylist.DenyListScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.Objects;

public abstract class SearchableListWidget<P extends DenyListScreen> extends ElementListWidget<SearchableListWidget<P>.Entry> {
    protected final P parent;
    protected String lastSearch;

    public SearchableListWidget(P parent, MinecraftClient client) {
        //(MinecraftClient client, int width, int height, int y, int itemHeight)
        this(parent,client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 23);
    }

    public SearchableListWidget(P parent,MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.parent = parent;

        this.updateEntries();
    }

    protected void updateEntries(){
        this.clearEntries();
    }

    protected void updateSearch(){
        String search = parent.getSearch();
        if(!search.equals(lastSearch)){
            lastSearch = search;
            this.updateEntries();
        }
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        this.updateSearch();

        //Render search entries
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int itemHeight = this.itemHeight - 9 - 1;
        int entryCount = this.getEntryCount();

        //Render regular entries
        for (int i = 0; i < entryCount; i++) {
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowBottom(i);
            if (rowBottom >= this.getY() && rowTop <= this.getBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, i, rowLeft, rowTop, rowWidth, itemHeight);
            }
        }
    }

    public abstract class Entry extends ElementListWidget.Entry<SearchableListWidget<P>.Entry> {
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(SearchableListWidget.this.getEntryAtPosition(mouseX, mouseY), this);
        }
    }
}
