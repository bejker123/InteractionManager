package com.bejker.interactionmanager.gui.options;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.config.option.interfaces.IItemOption;
import com.bejker.interactionmanager.config.option.interfaces.IRenderOption;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemOptionsScreen extends OptionsScreen {
    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.item_options");
    public ItemOptionsScreen(Screen parent) {
        super(parent,TITLE_TEXT);
        this.options_target = IItemOption.class;
    }

    @Override
    protected void addOptions() {
        if(this.body != null){
            List<ClickableWidget> option_widgets = Arrays.stream(Config.asOptions(this.options_target))
                    .map((x) -> x.createWidget(gameOptions)).toList();
            ArrayList<ClickableWidget> widgets = new ArrayList<>(option_widgets);
            this.body.addAll(widgets);
        }
    }

    @Override
    protected void reopen(){
        if(client == null){
            return;
        }
        this.close();
        client.setScreen(new ItemOptionsScreen(client.currentScreen));
    }


}
