package com.bejker.interactionmanager.gui.widget;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.gui.options.denylist.EntityDenyListScreen;
import com.bejker.interactionmanager.search.SearchUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.List;

public class EntityListWidget extends SearchableListWidget<EntityDenyListScreen> {

    public EntityListWidget(EntityDenyListScreen parent, MinecraftClient client) {
        super(parent,client);
    }

    protected void updateEntries() {
       super.updateEntries();
       if(lastSearch != null && !lastSearch.isBlank()){
           SearchUtil.searchEntities(lastSearch,-1,(x) -> !Config.DENIED_ENTITIES.contains(x)).stream()
           .map(SearchEntityEntry::new)
           .forEach(this::addEntry);
       }
       this.addEntry(new CategoryEntry(Text.translatable("category.interactionmanager.deny_listed_entities")));
       for (EntityType<?> i : Config.DENIED_ENTITIES){
          this.addEntry(new EntityEntry(i));
       }
    }

    public class EntityEntry extends EntityListWidget.Entry {
        public final Text block_name_text;
        public final Text block_id_text;
        private final ButtonWidget button;
        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/accept"),
                Identifier.ofVanilla("pending_invite/accept_highlighted")
        );

        public EntityEntry(EntityType<?> type){
           RegistryEntry<EntityType<?>> entry = Registries.ENTITY_TYPE.getEntry(type);
           this.block_name_text = type.getName();
           this.block_id_text = Text.of(entry.getIdAsString()).copy().withColor(Colors.GRAY);
           this.button = this.createButton(type);
        }
        ButtonWidget createButton(EntityType<?> type){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                Config.DENIED_ENTITIES.remove(type);
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

            context.drawTextWithShadow(EntityListWidget.this.client.textRenderer, this.block_name_text,ref_x,ref_y , Colors.WHITE);

            context.drawTextWithShadow(EntityListWidget.this.client.textRenderer, this.block_id_text, ref_x, ref_y + 10, Colors.GRAY);
            this.button.setX(x + entryWidth - this.button.getWidth() - 3);
            this.button.setY(ref_y - 1);
            this.button.render(context,mouseX,mouseY,tickDelta);
        }

    }

    public class SearchEntityEntry extends EntityEntry{

        private static final int lines = 2;

        public SearchEntityEntry(EntityType<?> type){
            super(type);
        }

        static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
                Identifier.ofVanilla("pending_invite/reject"),
                Identifier.ofVanilla("pending_invite/reject_highlighted")
        );
        @Override
        ButtonWidget createButton(EntityType<?> type){
            return new TexturedButtonWidget(20,20, BUTTON_TEXTURES,(button)->{
                Config.DENIED_ENTITIES.add(type);
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
            int text_width = EntityListWidget.this.client.textRenderer.getWidth(text);
            context.drawTextWithShadow(EntityListWidget.this.client.textRenderer,text,x +entryWidth / 2- text_width / 2,y + entryHeight / 4 + 2,Colors.WHITE);
        }
    }
}
