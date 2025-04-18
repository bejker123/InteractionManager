package com.bejker.interactionmanager.gui.widget;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.gui.options.blacklist.ItemBlockInteractionsScreen;
import com.bejker.interactionmanager.search.SearchUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemListWidget extends ElementListWidget<ItemListWidget.Entry> {
    private final ItemBlockInteractionsScreen parent;
    private String last_search = "";
    private Item selectedItem = null;

    private final HashSet<Item> searchEntryItems = new HashSet<>();

    public ItemListWidget(ItemBlockInteractionsScreen parent, MinecraftClient client) {
        super(client, parent.width / 2 - 5, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 23);
        this.parent = parent;

        this.updateEntries();
    }

    public @Nullable Item getSelectedItemOrNull(){
        return selectedItem;
    }

    public void updateEntries() {
       this.clearEntries();
       if(last_search != null && !last_search.isBlank()) {
           SearchUtil.searchItems(last_search).stream()
                   .distinct()
                   //.filter((x) -> !Config.BLACKLISTED_BLOCKS.contains(x))
                   //.sorted(Comparator.comparing((x) -> x.getName().getContent().visit(Optional::of).get().length()))
                   .map(SearchItemEntry::new)
                   .forEach(this::addSearchEntry);
       }
       if(this.getEntryCount() == 0) {
           for (Map.Entry<Item, HashSet<Block>> mapEntry : Config.DENIED_ITEM_INTERACTIONS.entrySet()) {
               if (mapEntry.getKey() != this.selectedItem) {
                   if (mapEntry.getValue() == null || mapEntry.getValue().isEmpty()) {
                       continue;
                   }
               }
               if (!searchEntryItems.contains(mapEntry.getKey())) {
                   this.addEntry(new ItemEntry(mapEntry.getKey()));
               }
           }
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

    private void addSearchEntry(SearchItemEntry searchItemEntry) {
        this.searchEntryItems.add(searchItemEntry.item);
        this.addEntry(searchItemEntry);
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

    @Override
    protected void clearEntries() {
        super.clearEntries();
        this.searchEntryItems.clear();
    }

    public abstract class Entry extends ElementListWidget.Entry<ItemListWidget.Entry> {
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(ItemListWidget.this.getEntryAtPosition(mouseX, mouseY), this);
        }
    }

    public class ItemEntry extends ItemListWidget.Entry {
        public final Text item_name_text;
        public final Text item_id_text;
        private final ButtonWidget button;
        final Item item;

        private static final int MAX_CHARS = 30;

        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/accept"),
                Identifier.ofVanilla("pending_invite/accept_highlighted")
        );

        public ItemEntry(Item item){
           RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
           this.item_name_text = Text.of(item.getName().asTruncatedString(MAX_CHARS));
           String id = entry.getIdAsString();
           this.item_id_text = Text.literal(id.substring(0,Math.min(id.length(),MAX_CHARS)));
           this.button = this.createButton(item);
           this.item = item;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ItemListWidget.this.selectedItem = ItemListWidget.this.selectedItem == this.item ? null : this.item;
            ItemListWidget.this.updateEntries();
            ItemListWidget.this.parent.updateBlacklistedBlocks();
            return super.mouseClicked(mouseX, mouseY, button);
        }

        ButtonWidget createButton(Item item){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                Config.BLACKLISTED_BLOCKS.remove(item);
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
            if(Config.RENDER_ITEMS_IN_BLOCK_BLACKLIST.getValue()){
               ref_x += 14;
            }
            int ref_y = y + entryHeight - 9;

            boolean isSelected = ItemListWidget.this.selectedItem == this.item;

            context.drawTextWithShadow(ItemListWidget.this.client.textRenderer, this.item_name_text,ref_x,ref_y ,isSelected ? 0xFF_FA_82_0A : Colors.WHITE);

            context.drawTextWithShadow(ItemListWidget.this.client.textRenderer, this.item_id_text, ref_x, ref_y + 10, Colors.GRAY);
            if(Config.RENDER_ITEMS_IN_BLOCK_BLACKLIST.getValue()){
                context.drawItemWithoutEntity(new ItemStack(item),ref_x - 20,ref_y);
            }
            if(isSelected){
                context.fill(x - 1,ref_y - 2,x + entryWidth - 3,ref_y + entryHeight * 2 - 6,0x20_0A_FA_0A);
                context.drawBorder(x - 2, ref_y - 3, entryWidth, entryHeight * 2 - 2, 0x0FB_0A_0B0);
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
                Config.DENIED_ITEM_INTERACTIONS.put(item, new HashSet<>());
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
