package com.bejker.interactionmanager.gui.widget;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.gui.options.denylist.ItemDenyListScreen;
import com.bejker.interactionmanager.search.SearchUtil;
import com.bejker.interactionmanager.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemDenyListWidget extends ElementListWidget<ItemDenyListWidget.Entry> {
    private final ItemDenyListScreen parent;
    private String last_search = "";

    public ItemDenyListWidget(ItemDenyListScreen parent, MinecraftClient client) {
	    //(MinecraftClient client, int width, int height, int y, int itemHeight)
        super(client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 23);
        this.parent = parent;

        this.updateEntries();
    }

    private void updateEntries() {
       this.clearEntries();
       if(last_search != null && !last_search.isBlank()){
           SearchUtil.searchItems(last_search,-1,(Item item) -> {
                       try {
                           return Util.doesOverrideMethod(item.getClass(),"use",Item.class) && !BlockItem.class.isAssignableFrom(item.getClass());
                       } catch (NoSuchMethodException e) {
                           e.printStackTrace();
                       }
                       return false;
                   }).stream()
            .filter((x) -> !Config.DENIED_ITEMS.contains(x))
           .map(SearchItemEntry::new)
           .forEach(this::addEntry);
       }
       for (Item i : Config.DENIED_ITEMS){
          this.addEntry(new ItemEntry(i));
       }

       //It should be impossible, but better add this check now then debug this in the future,
       //when it could be possible
       if(this.getEntryCount() == 0){
           return;
       }
       //if(this.getScrollY() > this.getRowBottom(this.getEntryCount() - 1)){
       //    this.setScrollY(this.getRowBottom(this.getEntryCount() - 1));
       //}
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

        //Render regular entries
        for (int i = 0; i < entryCount; i++) {
            int rowTop = this.getRowTop(i);
            int rowBottom = this.getRowBottom(i);
            if (rowBottom >= this.getY() && rowTop <= this.getBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, i, rowLeft, rowTop, rowWidth, itemHeight);
            }
        }
    }

    public Optional<Element> hoveredElement(double mouseX, double mouseY) {
        for (Element element : this.children()) {
            if (element.isMouseOver(mouseX, mouseY)) {
                return Optional.of(element);
            }
        }

        return Optional.empty();
    }
    public abstract class Entry extends ElementListWidget.Entry<ItemDenyListWidget.Entry> {
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(ItemDenyListWidget.this.getEntryAtPosition(mouseX, mouseY), this);
        }
    }

    public class ItemEntry extends ItemDenyListWidget.Entry {
        public final Text item_name_text;
        public final Text item_id_text;
        private final ButtonWidget button;
        private final Item item;

        private static final int MAX_CHARS = 30;

        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/accept"),
                Identifier.ofVanilla("pending_invite/accept_highlighted")
        );

        public ItemEntry(Item item){
           RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
           this.item_name_text = Text.of(item.getName().asTruncatedString(MAX_CHARS));
           String id = entry.getIdAsString();
           this.item_id_text = Text.literal(id.substring(0,Math.min(id.length(),MAX_CHARS))).withColor(Colors.GRAY);
           this.button = this.createButton(item);
           this.item = item;
        }
        ButtonWidget createButton(Item item){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                Config.DENIED_ITEMS.remove(item);
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
            if(Config.RENDER_ITEMS_IN_BLOCK_DENY_LIST.getValue()){
               ref_x += 14;
            }
            int ref_y = y + entryHeight - 9;

            context.drawTextWithShadow(ItemDenyListWidget.this.client.textRenderer, this.item_name_text,ref_x,ref_y , Colors.WHITE);

            context.drawTextWithShadow(ItemDenyListWidget.this.client.textRenderer, this.item_id_text, ref_x, ref_y + 10, Colors.GRAY);
            this.button.setX(x + entryWidth - this.button.getWidth() - 3);
            this.button.setY(ref_y - 1);
            this.button.render(context,mouseX,mouseY,tickDelta);
            if(Config.RENDER_ITEMS_IN_BLOCK_DENY_LIST.getValue()){
                context.drawItemWithoutEntity(new ItemStack(item),ref_x - 20,ref_y);
            }
        }

    }

    public class SearchItemEntry extends ItemEntry{

        private static final int lines = 2;

        public SearchItemEntry(Item item){
            super(item);
        }

        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/reject"),
                Identifier.ofVanilla("pending_invite/reject_highlighted")
        );
        @Override
        ButtonWidget createButton(Item item){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                Config.DENIED_ITEMS.add(item);
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
}
