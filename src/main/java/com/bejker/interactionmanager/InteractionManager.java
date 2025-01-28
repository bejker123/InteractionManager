package com.bejker.interactionmanager;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractionManager implements ModInitializer {

    public static final String MOD_ID = "interactionmanager";

    public static Identifier id(String s){
        return Identifier.of(MOD_ID,s);
    }
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
    }
}
