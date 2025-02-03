package com.bejker.interactionmanager.client.gui.widget;

import com.bejker.interactionmanager.client.config.Config;
import com.bejker.interactionmanager.client.gui.BlockBlacklistScreen;
import com.bejker.interactionmanager.client.search.SearchUtil;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.*;

public class BlockListWidget extends ElementListWidget<BlockListWidget.Entry> {
    private final BlockBlacklistScreen parent;
    private String last_search = "";

    private final List<SearchBlockEntry> searchBlockEntries = new ArrayList<>();
    public BlockListWidget(BlockBlacklistScreen parent,MinecraftClient client) {
	    //(MinecraftClient client, int width, int height, int y, int itemHeight)
        super(client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 23);
        this.parent = parent;

        this.updateEntries();
    }

    @Override
    protected void clearEntries() {
        super.clearEntries();
        searchBlockEntries.clear();
    }

    private void updateEntries() {
       this.clearEntries();
       if(last_search != null && !last_search.isBlank()){
           SearchUtil.searchBlocks(last_search,-1).stream()
           .filter((x) -> !Config.BLACKLISTED_BLOCKS.contains(x))
           .map(SearchBlockEntry::new)
           .toList().forEach(this::addEntry);
       }
       this.addEntry(new CategoryEntry(Text.translatable("category.interactionmanager.blacklisted")));
       for (Block i : Config.BLACKLISTED_BLOCKS){
          this.addEntry(new BlockEntry(i));
       }
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        String search = parent.getSearch();
        if(!search.equals(last_search)){
            last_search = search;
            this.updateEntries();
        }

        //Render search entries
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int itemHeight = this.itemHeight - 9 - 1;
        int entryCount = this.getEntryCount();

        int i = 0;
        for (; i < searchBlockEntries.size(); i++) {
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowBottom(i);
            if (rowBottom >= this.getY() && rowTop <= this.getBottom()) {
                SearchBlockEntry entry = searchBlockEntries.get(i);
                boolean isHovered = Objects.equals(this.getHoveredEntry(),entry);
                entry.drawBorder(context,i,rowTop,rowLeft,rowWidth,itemHeight,mouseX,mouseY,isHovered,delta);
                entry.render(context,i,rowTop,rowLeft,rowWidth,itemHeight,mouseX,mouseY,isHovered,delta);
            }
        }

        //Render regular entries
        for (; i < entryCount + searchBlockEntries.size(); i++) {
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowBottom(i);
            if (rowBottom >= this.getY() && rowTop <= this.getBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, i - searchBlockEntries.size(), rowLeft, rowTop, rowWidth, itemHeight);
            }
        }
    }

    public Optional<Element> hoveredElement(double mouseX, double mouseY) {
        for (Element element : this.searchBlockEntries) {
            if (element.isMouseOver(mouseX, mouseY)) {
                return Optional.of(element);
            }
        }

        for (Element element : this.children()) {
            if (element.isMouseOver(mouseX, mouseY)) {
                return Optional.of(element);
            }
        }

        return Optional.empty();
    }
    public abstract class Entry extends ElementListWidget.Entry<BlockListWidget.Entry> {
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(BlockListWidget.this.getEntryAtPosition(mouseX, mouseY), this);
        }
    }

    public class BlockEntry extends BlockListWidget.Entry {
        public final Text block_name_text;
        public final Text block_id_text;
        private final ButtonWidget button;
        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/accept"),
                Identifier.ofVanilla("pending_invite/accept_highlighted")
        );

        public BlockEntry(Block block){
           RegistryEntry<Block> entry = Registries.BLOCK.getEntry(block);
           this.block_name_text = block.getName();
           this.block_id_text = Text.of(entry.getIdAsString()).copy().withColor(Colors.GRAY);
           this.button = this.createButton(block);
        }
        ButtonWidget createButton(Block block){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                Config.BLACKLISTED_BLOCKS.remove(block);
                updateEntries();
            },Text.translatable("button.interactionmanager.remove"));
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(button);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(button);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int ref_x = x + entryWidth / 32;
            int ref_y = y + entryHeight - 9;

            context.drawTextWithShadow(BlockListWidget.this.client.textRenderer, this.block_name_text,ref_x,ref_y , Colors.WHITE);

            context.drawTextWithShadow(BlockListWidget.this.client.textRenderer, this.block_id_text, ref_x, ref_y + 10, Colors.GRAY);
            this.button.setX(x + entryWidth - this.button.getWidth() - 3);
            this.button.setY(ref_y - 1);
            this.button.render(context,mouseX,mouseY,tickDelta);
        }

    }

    public class SearchBlockEntry extends BlockEntry{

        private static final int lines = 2;

        public SearchBlockEntry(Block block){
            super(block);
        }

        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/reject"),
                Identifier.ofVanilla("pending_invite/reject_highlighted")
        );
        @Override
        ButtonWidget createButton(Block block){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                Config.BLACKLISTED_BLOCKS.add(block);
                updateEntries();
            },Text.translatable("button.interactionmanager.remove"));
        }

        @Override
        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int ref_y = y + entryHeight - 9 - 1;
            context.drawBorder(x - 2, ref_y - 2, entryWidth, entryHeight * lines - 2, 0x0FBABABA);
            context.fill(x - 1,ref_y - 1,x + entryWidth - 3,ref_y + entryHeight * lines - 4,0x10_AA_AA_AA);
        }

    }

    public class CategoryEntry extends Entry{

        private final Text text;

        public CategoryEntry(Text text){
           this.text = text;
        }

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
            int text_width = BlockListWidget.this.client.textRenderer.getWidth(text);
            context.drawTextWithShadow(BlockListWidget.this.client.textRenderer,text,x +entryWidth / 2- text_width / 2,y + entryHeight / 4 + 2,Colors.WHITE);
        }
    }
}
