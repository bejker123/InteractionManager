package com.bejker.interactionmanager.comp.modmenu;
import com.bejker.interactionmanager.gui.OptionsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
public class ModMenuApiImpl implements ModMenuApi{

    public ConfigScreenFactory<OptionsScreen> getModConfigScreenFactory() {
        return OptionsScreen::new;
    }
}
