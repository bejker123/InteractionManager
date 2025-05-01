package com.bejker.interactionmanager.gui.widget;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.gui.options.denylist.ItemInteractionsScreen;
import com.bejker.interactionmanager.search.SearchUtil;
import com.bejker.interactionmanager.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.*;

public class ItemInteractionListWidget extends ElementListWidget<ItemInteractionListWidget.Entry> {
    private final ItemInteractionsScreen parent;
    private String last_search = "";

    public ItemInteractionListWidget(ItemInteractionsScreen parent, MinecraftClient client) {
        super(client, parent.width / 2 - 5, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 23);
        this.parent = parent;

        this.updateEntries();
    }

    public void updateEntries() {
       this.clearEntries();
       if(this.parent.getSelectedItem() == null){
           return;
       }
       boolean isAllSelected = Items.AIR == this.parent.getSelectedItem();
       boolean overridesUseOnBlock = isAllSelected || Util.doesOverrideMethod(this.parent.getSelectedItem().getClass(),"useOnBlock", Item.class);
       boolean overridesUse = isAllSelected || Util.doesOverrideMethod(this.parent.getSelectedItem().getClass(),"use", Item.class);

       if(overridesUseOnBlock&& last_search != null&& !last_search.isBlank()){
           SearchUtil.searchBlocks(last_search,-1,x -> !x.equals(Blocks.AIR)).stream()
           .distinct()
           //.filter((x) -> !Config.DENIED_ITEM_INTERACTIONS.getOrDefault(parent.getSelectedItem(),new HashSet<>()).contains(x))
           //.sorted(Comparator.comparing((x) -> x.getName().getContent().visit(Optional::of).get().length()))
           .map(SearchBlockEntry::new)
           .forEach(this::addEntry);
       }else{
           if(overridesUseOnBlock){
               this.addEntry(new AllEntry());
           }
           if(overridesUse){
               this.addEntry(new UseEntry());
           }
           HashSet<Block> blocks = Config.DENIED_ITEM_INTERACTIONS.get(parent.getSelectedItem());
           if(blocks != null){
               for (Block i : blocks){
                   if(i == Blocks.AIR){
                       continue;
                   }
                   this.addEntry(new BlockEntry(i));
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

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        String search = parent.getBlockSearch();
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
    public abstract class Entry extends ElementListWidget.Entry<ItemInteractionListWidget.Entry> {
        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return Objects.equals(ItemInteractionListWidget.this.getEntryAtPosition(mouseX, mouseY), this);
        }
    }

    public class BlockEntry extends ItemInteractionListWidget.Entry {
        public Text block_name_text;
        public Text block_id_text;
        private final ButtonWidget button;
        private final Block block;

        private static final int MAX_CHARS = 30;

        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/accept"),
                Identifier.ofVanilla("pending_invite/accept_highlighted")
        );

        public BlockEntry(Block block){
           RegistryEntry<Block> entry = Registries.BLOCK.getEntry(block);
           this.block_name_text = Text.of(block.getName().asTruncatedString(MAX_CHARS));
           String id = entry.getIdAsString();
           this.block_id_text = Text.literal(id.substring(0,Math.min(id.length(),MAX_CHARS))).withColor(Colors.GRAY);
           this.button = this.createButton(block);
           this.block = block;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        ButtonWidget createButton(Block block){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                removeEntry(block);
            },Text.translatable("button.interactionmanager.remove"));
        }

        protected void removeEntry(Block block){
            if(ItemInteractionListWidget.this.parent.getSelectedItem() == null){
                return;
            }
            HashSet<Block> deniedBlocks = Config.DENIED_ITEM_INTERACTIONS.get(ItemInteractionListWidget.this.parent.getSelectedItem());
            if(deniedBlocks != null){
                deniedBlocks.remove(block);
            }
            Config.DENIED_ITEM_INTERACTIONS.put(ItemInteractionListWidget.this.parent.getSelectedItem(),deniedBlocks);
            updateEntries();
            ItemInteractionListWidget.this.parent.updateItems();
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

            context.drawTextWithShadow(ItemInteractionListWidget.this.client.textRenderer, this.block_name_text,ref_x,ref_y , Colors.WHITE);

            context.drawTextWithShadow(ItemInteractionListWidget.this.client.textRenderer, this.block_id_text, ref_x, ref_y + 10, Colors.GRAY);
            this.button.setX(x + entryWidth - this.button.getWidth() - 3);
            this.button.setY(ref_y - 1);
            this.button.render(context,mouseX,mouseY,tickDelta);
            if(Config.RENDER_ITEMS_IN_BLOCK_DENY_LIST.getValue()){
                context.drawItemWithoutEntity(new ItemStack(block),ref_x - 20,ref_y);
            }
        }
    }

    public class SearchBlockEntry extends BlockEntry{

        private static final int lines = 2;
        protected int borderColor = 0x0FBABABA;
        protected int bgColor = 0x10_AA_AA_AA;

        public SearchBlockEntry(Block block){
            super(block);
        }

        static final ButtonTextures BUTTON_TEXTURES_REJECT = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/reject"),
                Identifier.ofVanilla("pending_invite/reject_highlighted")
        );

        static final ButtonTextures BUTTON_TEXTURES_ACCEPT = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/accept"),
                Identifier.ofVanilla("pending_invite/accept_highlighted")
        );
        @Override
        ButtonWidget createButton(Block block){
            HashSet<Block> initiallyDeniedBlocks = Config.DENIED_ITEM_INTERACTIONS.get(ItemInteractionListWidget.this.parent.getSelectedItem());
            boolean initialButtonState = initiallyDeniedBlocks == null || !initiallyDeniedBlocks.contains(block);

            return new TexturedButtonWidget(20,20, initialButtonState ? BUTTON_TEXTURES_REJECT : BUTTON_TEXTURES_ACCEPT,(button)->{
                if(initialButtonState){
                    addEntry(block);
                }else{
                    removeEntry(block);
                }
            },Text.translatable("button.interactionmanager.remove"));
        }

        void addEntry(Block block){
            if(ItemInteractionListWidget.this.parent.getSelectedItem() == null){
                return;
            }
            HashSet<Block> deniedBlocks = Config.DENIED_ITEM_INTERACTIONS.get(ItemInteractionListWidget.this.parent.getSelectedItem());
            if(deniedBlocks == null){
                deniedBlocks = new HashSet<>();
            }
            deniedBlocks.add(block);
            Config.DENIED_ITEM_INTERACTIONS.put(ItemInteractionListWidget.this.parent.getSelectedItem(),deniedBlocks);
            ItemInteractionListWidget.this.parent.updateItems();
            updateEntries();
        }

        @Override
        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int ref_y = y + entryHeight - 9 - 1;
            context.drawBorder(x - 2, ref_y - 2, entryWidth, entryHeight * lines - 2, borderColor);
            context.fill(x - 1,ref_y - 1,x + entryWidth - 3,ref_y + entryHeight * lines - 5,bgColor);
        }

    }

    public class AllEntry extends SearchBlockEntry{
        public AllEntry() {
            super(Blocks.AIR);
            this.block_name_text = Text.translatable("text.interactionmanager.deny_using_on_all_blocks.title");
            this.block_id_text = Text.translatable("text.interactionmanager.deny_using_on_all_blocks.tooltip");
            this.borderColor = 0xFC_AA_AA_AA;
            this.bgColor = 0xCB_0F_0F_0F;
        }
    }

    private class UseEntry extends AllEntry {
        public UseEntry() {
            this.block_name_text = Text.translatable("text.interactionmanager.deny_using_item.title");
            this.block_id_text = Text.translatable("text.interactionmanager.deny_using_item.tooltip");
            this.borderColor = 0xFC_AA_AA_AA;
            this.bgColor = 0xCB_0F_0F_0F;
        }

        @Override
        ButtonWidget createButton(Block block){
            boolean initialButtonState = !Config.DENIED_ITEMS.contains(ItemInteractionListWidget.this.parent.getSelectedItem());

            return new TexturedButtonWidget(20,20, initialButtonState ? BUTTON_TEXTURES_REJECT : BUTTON_TEXTURES_ACCEPT,(button)->{
                if(ItemInteractionListWidget.this.parent.getSelectedItem() == null){
                    return;
                }
                if(initialButtonState){
                    Config.DENIED_ITEMS.add(ItemInteractionListWidget.this.parent.getSelectedItem());
                }else{
                    Config.DENIED_ITEMS.remove(ItemInteractionListWidget.this.parent.getSelectedItem());
                }
                ItemInteractionListWidget.this.updateEntries();
            },Text.translatable("button.interactionmanager.remove"));
        }
    }
}
