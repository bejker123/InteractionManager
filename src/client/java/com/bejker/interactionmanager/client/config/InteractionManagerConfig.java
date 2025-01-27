package com.bejker.interactionmanager.client.config;

import com.bejker.interactionmanager.InteractionManager;
import com.bejker.interactionmanager.client.config.option.BooleanOption;
import com.bejker.interactionmanager.client.config.option.IOptionConvertable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.SimpleOption;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

public class InteractionManagerConfig {
    private static Path config_path;
    public static final BooleanOption ALLOW_SHOVEL_CREATE_PATHS = new BooleanOption("allow_shovel_create_paths");
    public static final BooleanOption ALLOW_AXE_STRIP_BLOCKS = new BooleanOption("allow_axe_strip_blocks");
    public static final BooleanOption ALLOW_USE_FIREWORK_ON_BLOCK = new BooleanOption("allow_use_firework_on_block");
    public static final BooleanOption ALLOW_ATTACKING_HOSTILE_ENTITIES = new BooleanOption("allow_attacking_hostile_entities");
    public static final BooleanOption ALLOW_ATTACKING_PASSIVE_ENTITIES = new BooleanOption("allow_attacking_passive_entities");

    private static void setupConfigFile(){
        if(config_path != null){
            return;
        }
        config_path = FabricLoader.getInstance().getConfigDir().resolve(InteractionManager.MOD_ID + ".json");
    }

    public static SimpleOption<?>[] asOptions() {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();
        for (Field field : InteractionManagerConfig.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) &&
                    IOptionConvertable.class.isAssignableFrom(field.getType())) {
                try {
                    options.add(((IOptionConvertable) field.get(null)).asOption());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return options.stream().toArray(SimpleOption[]::new);
    }

    public static void loadConfig(){
        setupConfigFile();

        if (!Files.exists(config_path)) {
            saveConfig();
            return;
        }
        try{
            BufferedReader reader = Files.newBufferedReader(config_path);
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for(Field field: InteractionManagerConfig.class.getDeclaredFields()){
                String field_name = field.getName().toLowerCase(Locale.ROOT);
                if (BooleanOption.class.isAssignableFrom(field.getType())) {
                    BooleanOption option = (BooleanOption) field.get(null) ;
                    JsonPrimitive value = json.getAsJsonPrimitive(field_name);
                    if(value == null){
                        continue;
                    }
                    option.setValue(value.getAsBoolean());
                }
            }

        }catch (IOException | IllegalAccessException e){
            System.err.println("Couldn't load Interaction Manager config, using defaults.");
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        setupConfigFile();
        JsonObject config = new JsonObject();

        try {
            for (Field field : InteractionManagerConfig.class.getDeclaredFields()) {
                String field_name = field.getName().toLowerCase(Locale.ROOT);
                if (BooleanOption.class.isAssignableFrom(field.getType())) {
                    BooleanOption option = (BooleanOption) field.get(null) ;
                    config.addProperty(field_name, option.getValue());
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
