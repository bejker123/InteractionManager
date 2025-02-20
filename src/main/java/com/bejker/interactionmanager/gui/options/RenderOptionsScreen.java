package com.bejker.interactionmanager.gui.options;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.config.option.interfaces.IEntityOption;
import com.bejker.interactionmanager.config.option.interfaces.IRenderOption;
import com.bejker.interactionmanager.gui.options.blacklist.EntityBlacklistScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RenderOptionsScreen extends OptionsScreen {
    private static final Text TITLE_TEXT = Text.translatable("screen.interactionmanager.render_options");
    public RenderOptionsScreen(Screen parent) {
        super(parent,TITLE_TEXT);
        this.options_target = IRenderOption.class;
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
        client.setScreen(new RenderOptionsScreen(client.currentScreen));
    }


}
