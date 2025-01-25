package com.bejker.interactionmanager.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;

import java.util.List;

import static com.bejker.interactionmanager.client.InteractionManagerClient.CLIENT_LOGGER;

public class GuiCallbacks {
    public static void gameMenuOnInitWidgets(Screen screen, GridWidget gridWidget){

    }

    public static void optionsScreenOnInitWidgets(Screen screen, GridWidget gridWidget){
        for (int i = 0; i < gridWidget.children.size(); i++) {
           ButtonWidget button = (ButtonWidget) gridWidget.children.get(i);
           CLIENT_LOGGER.info("{}",button.getMessage());
        }
    }
}

