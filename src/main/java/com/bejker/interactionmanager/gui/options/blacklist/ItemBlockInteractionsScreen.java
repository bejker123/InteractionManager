package com.bejker.interactionmanager.gui.options.blacklist;

import com.bejker.interactionmanager.gui.widget.BlockInteractionListWidget;
import com.bejker.interactionmanager.gui.widget.ItemListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.Locale;

public class ItemBlockInteractionsScreen extends BlacklistScreen{

    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.item_block_interaction_blacklist");

    private ItemListWidget itemListWidget;
    private BlockInteractionListWidget blockInteractionListWidget;

    private TextFieldWidget blockSearch;

    public ItemBlockInteractionsScreen(Screen parent) {
        super(parent, TITLE_TEXT);
        // We have 2 searchbars, if one would be focused on a key-press, then the other one couldn't be used at all.
        this.focusSearchOnKeyPress = false;
    }

    @Override
    protected void initHeader() {
        DirectionalLayoutWidget headerWidget = new DirectionalLayoutWidget(0,0, DirectionalLayoutWidget.DisplayAxis.VERTICAL);
        headerWidget.getMainPositioner().margin(0,5);
        headerWidget.getMainPositioner().alignVerticalCenter();
        headerWidget.setY(0);

        titleWidget = new TextWidget(title,textRenderer);
        titleWidget.alignCenter();
        headerWidget.add(titleWidget);

        headerWidget.refreshPositions();

        DirectionalLayoutWidget searchBarsWidget = new DirectionalLayoutWidget(0,0, DirectionalLayoutWidget.DisplayAxis.HORIZONTAL);
        searchBarsWidget.spacing(100);

        search = new TextFieldWidget(textRenderer,150,20,Text.empty());
        search.setPlaceholder(Text.literal("item..."));
        searchBarsWidget.add(search);

        blockSearch = new TextFieldWidget(textRenderer,150,20,Text.empty());
        blockSearch.setPlaceholder(Text.literal("block..."));
        blockSearch.setUneditableColor(Colors.GRAY);
        searchBarsWidget.add(blockSearch);

        headerWidget.add(searchBarsWidget);

        this.layout.addHeader(headerWidget);
        this.layout.setHeaderHeight(this.layout.getHeaderHeight() + 20);
    }

    @Override
    protected void initBody() {
        DirectionalLayoutWidget widget = new DirectionalLayoutWidget(0,0, DirectionalLayoutWidget.DisplayAxis.HORIZONTAL);

        itemListWidget = widget.add(new ItemListWidget(this, this.client));
        blockInteractionListWidget = widget.add(new BlockInteractionListWidget(this, this.client));

        this.layout.addBody(widget);
    }

    @Override
    public void tick() {
        super.tick();

        this.blockSearch.active = this.getSelectedItem() != null;
        this.blockInteractionListWidget.active = this.getSelectedItem() != null;

        if(!this.blockSearch.active){
            this.blockSearch.setTooltip(Tooltip.of(Text.translatable("search.interactionmanager.block_search.tooltip.inactive")));
            this.blockSearch.setFocused(false);

            this.blockInteractionListWidget.setTooltip(Tooltip.of(Text.translatable("search.interactionmanager.block_search.tooltip.inactive")));
            this.blockInteractionListWidget.setFocused(false);
        }else{
            this.blockSearch.setTooltip(Tooltip.of(Text.empty()));
            this.blockInteractionListWidget.setTooltip(Tooltip.of(Text.empty()));
        }
        refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();

        this.itemListWidget.position(this.width / 2 - 5, this.layout);
        this.itemListWidget.setX(2);

        this.blockInteractionListWidget.position(this.width / 2 - 5, this.layout);
        this.blockInteractionListWidget.setX(this.width / 2);

    }

    public String getBlockSearch() {
        return this.blockSearch.getText().strip().toLowerCase(Locale.ROOT);
    }

    public Item getSelectedItem() {
        return this.itemListWidget.getSelectedItemOrNull();
    }

    public void updateBlacklistedBlocks() {
        this.blockInteractionListWidget.updateEntries();
    }

    public void updateItems() {
        this.itemListWidget.updateEntries();
    }
}
