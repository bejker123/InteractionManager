package com.bejker.interactionmanager.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.OptionListWidget;

import java.util.List;
import java.util.Optional;

public class ListWidget extends ElementListWidget<ListWidget.WidgetEntry> {
    public ListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }


    protected static class WidgetEntry extends ElementListWidget.Entry<ListWidget.WidgetEntry> {
        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

        }
    }


}
