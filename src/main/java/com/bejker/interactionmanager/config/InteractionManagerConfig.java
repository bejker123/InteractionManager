package com.bejker.interactionmanager.config;

import com.bejker.interactionmanager.InteractionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class InteractionManagerConfig {
    private static final InteractionManagerConfig instance = new InteractionManagerConfig();
    private static Path config_path;
    public boolean ALLOW_SHOVEL_CREATE_PATHS = true;
    public boolean ALLOW_AXE_STRIP_BLOCKS = true;

    public static InteractionManagerConfig getInstance() {
        return instance;
    }

    private InteractionManagerConfig(){
    }

    private static void setupConfigFile(){
        if(config_path != null){
            return;
        }
        config_path = FabricLoader.getInstance().getConfigDir().resolve(InteractionManager.MOD_ID + ".json");
    }
    public void loadConfig(){
        setupConfigFile();

        if (!Files.exists(config_path)) {
            saveConfig();
            return;
        }
        try{
            BufferedReader reader = Files.newBufferedReader(config_path);
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for(Field field: InteractionManagerConfig.class.getDeclaredFields()){
                if(Modifier.isStatic(field.getModifiers())){
                    continue;
                }
                String field_name = field.getName().toLowerCase(Locale.ROOT);
                if(field.getType() == boolean.class){
                    JsonPrimitive value = json.getAsJsonPrimitive(field_name);
                    if(value == null){
                        continue;
                    }
                    field.set(this,value.getAsBoolean());
                }
            }

        }catch (IOException | IllegalAccessException e){
            System.err.println("Couldn't load Interaction Manager config, using defaults.");
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        setupConfigFile();
        JsonObject config = new JsonObject();

        try {
            for (Field field : InteractionManagerConfig.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                String field_name = field.getName().toLowerCase(Locale.ROOT);
                if (field.getType() == boolean.class) {
                    config.addProperty(field_name, field.getBoolean(this));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        String string = new Gson().toJson(config);

        try {
            BufferedWriter writer = Files.newBufferedWriter(config_path);
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            System.err.println("Couldn't load Interaction Manager config, using defaults.");
            e.printStackTrace();
        }
    }

}
