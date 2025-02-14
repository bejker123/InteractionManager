package com.bejker.interactionmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onInitialize() {
    }
}
