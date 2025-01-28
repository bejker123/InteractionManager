package com.bejker.interactionmanager.client.comp.modmenu;
import com.bejker.interactionmanager.client.gui.OptionsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
public class ModMenuApiImpl implements ModMenuApi{

    public ConfigScreenFactory<OptionsScreen> getModConfigScreenFactory() {
        return OptionsScreen::new;
    }
}
