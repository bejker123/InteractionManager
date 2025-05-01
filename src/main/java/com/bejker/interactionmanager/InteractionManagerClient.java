package com.bejker.interactionmanager;

import com.bejker.interactionmanager.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;

public class InteractionManagerClient implements ClientModInitializer {
    IMReloadListener imReloadListener = new IMReloadListener();

    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();
        ConfigManager.initRuntimeOptions();

        imReloadListener.init();
    }
}
