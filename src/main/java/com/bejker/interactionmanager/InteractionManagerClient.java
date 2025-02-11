package com.bejker.interactionmanager;

import com.bejker.interactionmanager.config.ConfigManager;
import com.bejker.interactionmanager.search.SearchUtil;
import net.fabricmc.api.ClientModInitializer;

public class InteractionManagerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();
        ConfigManager.initRuntimeOptions();
    }
}
