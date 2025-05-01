package com.bejker.interactionmanager.gui.widget;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.gui.options.denylist.ItemInteractionsScreen;
import com.bejker.interactionmanager.search.SearchUtil;
import com.bejker.interactionmanager.util.Util;
import net.minecraft.block.Block;
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
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemListWidget extends ElementListWidget<ItemListWidget.Entry> {
    private final ItemInteractionsScreen parent;
    private String lastSearch = "";
    private static Item selectedItem = null;

    private final HashSet<Item> searchEntryItems = new HashSet<>();

    public ItemListWidget(ItemInteractionsScreen parent, MinecraftClient client) {
        super(client, parent.width / 2 - 5, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 23);
        this.parent = parent;

        this.updateEntries();
    }

    public @Nullable Item getSelectedItemOrNull(){
        return selectedItem;
    }

    public void updateEntries() {
       this.clearEntries();
       HashSet<Item> blockEntryItems = new HashSet<>();
       HashSet<Item> deniedEntryItems = new HashSet<>();
       if(lastSearch != null && !lastSearch.isBlank()) {
           SearchUtil.searchItems(lastSearch,-1,(Item item) -> {
                       //TODO: add config option for blocks
                       return Util.doesOverrideMethod(item.getClass(),"use",Item.class)||
                               Util.doesOverrideMethod(item.getClass(),"useOnBlock",Item.class)||
                               BlockItem.class.isAssignableFrom(item.getClass());
                   }).stream()
                   .distinct()
                   .map(SearchItemEntry::new)
                   .forEach(this::addSearchEntry);
       }else {
           this.addEntry(new AllEntry());
           for (Map.Entry<Item, HashSet<Block>> mapEntry : Config.DENIED_ITEM_INTERACTIONS.entrySet()) {
               Item item = mapEntry.getKey();
               HashSet<Block> blocks = mapEntry.getValue();

               if (item != selectedItem && (blocks == null || blocks.isEmpty())) {
                   continue;
               }
               if (item != Items.AIR) {
                   this.addEntry(new ItemEntry(item));
                   blockEntryItems.add(item);
               }
           }
           for (Item item : Config.DENIED_ITEMS){
               if (item != selectedItem&&item == null) {
                   continue;
               }
               if (item != Items.AIR&&!blockEntryItems.contains(item)) {
                   this.addEntry(new ItemEntry(item));
                   deniedEntryItems.add(item);
               }
           }
       }
       if(!blockEntryItems.contains(selectedItem)&&!deniedEntryItems.contains(selectedItem)&&
               this.searchEntryItems.isEmpty()&&selectedItem != null&&selectedItem != Items.AIR){
               this.addEntry(new ItemEntry(selectedItem));
       }
    }

    private void addSearchEntry(SearchItemEntry searchItemEntry) {
        this.searchEntryItems.add(searchItemEntry.item);
        this.addEntry(searchItemEntry);
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        String search = parent.getSearch();
        if(!search.equals(lastSearch)){
            lastSearch = search;
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
        public Text item_name_text;
        public Text item_id_text;
        final Item item;

        private static final int MAX_CHARS = 30;

        // Path: C:\Users\\user\code\InteractionManager\.gradle\loom-cache\minecraftMaven\net\minecraft\minecraft-merged-3344e8d46b\1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2\minecraft-merged-3344e8d46b-1.21-net.fabricmc.yarn.1_21.1.21+build.9-v2.jar!\assets\minecraft\textures\gui\sprites
        public ItemEntry(Item item){
           RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);
           this.item_name_text = Text.of(item.getName().asTruncatedString(MAX_CHARS));
           String id = entry.getIdAsString();
           this.item_id_text = Text.literal(id.substring(0,Math.min(id.length(),MAX_CHARS)));
           this.item = item;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            selectedItem = selectedItem == this.item ? null : this.item;
            ItemListWidget.this.updateEntries();
            ItemListWidget.this.parent.updateBlacklistedBlocks();
            return true;
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
            int ref_x = x + entryWidth / 32;
            if(Config.RENDER_ITEMS_IN_BLOCK_DENY_LIST.getValue()){
               ref_x += 14;
            }
            int ref_y = y + entryHeight - 9;

            boolean isSelected = selectedItem == this.item;
            context.drawTextWithShadow(ItemListWidget.this.client.textRenderer, this.item_name_text,ref_x,ref_y ,isSelected ? 0xFF_FA_82_0A : Colors.WHITE);
            context.drawTextWithShadow(ItemListWidget.this.client.textRenderer, this.item_id_text, ref_x, ref_y + 10, Colors.GRAY);
            if(Config.RENDER_ITEMS_IN_BLOCK_DENY_LIST.getValue()){
                context.drawItemWithoutEntity(new ItemStack(item),ref_x - 20,ref_y);
            }
            if(isSelected){
                context.fill(x - 1,ref_y - 2,x + entryWidth - 3,ref_y + entryHeight * 2 - 6,0x20_0A_FA_0A);
                context.drawBorder(x - 2, ref_y - 3, entryWidth, entryHeight * 2 - 2, 0xFC_AA_AA_AA);
            }
        }
    }

    public class SearchItemEntry extends ItemEntry{

        private static final int lines = 2;
        protected int bgColor = 0x10_AA_AA_AA;

        public SearchItemEntry(Item item){
            super(item);
        }

        @Override
        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int ref_y = y + entryHeight - 9 - 1;
            boolean isSelected = selectedItem == this.item;
            if(!isSelected){
                context.drawBorder(x - 2, ref_y - 2, entryWidth, entryHeight * lines - 2, 0x0FBABABA);
            }
            context.fill(x - 1,ref_y - 1,x + entryWidth - 3,ref_y + entryHeight * lines - 4,this.bgColor);
        }

    }

    public class AllEntry extends SearchItemEntry {
        public AllEntry() {
            super(Items.AIR);
            this.item_name_text = Text.translatable("text.interactionmanager.deny_using_all_items.title");
            this.item_id_text = Text.translatable("text.interactionmanager.deny_using_all_items.tooltip");
            this.bgColor = 0xCB_0F_0F_0F;
        }
    }
}
