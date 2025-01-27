package com.bejker.interactionmanager.client.config;

import com.bejker.interactionmanager.InteractionManager;
import com.bejker.interactionmanager.client.config.option.BooleanOption;
import com.bejker.interactionmanager.client.config.option.ConfigStorage;
import com.bejker.interactionmanager.client.config.option.EnumOption;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
                } else if (EnumOption.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType) {
                    JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive(field.getName()
                            .toLowerCase(Locale.ROOT));
                    if (jsonPrimitive != null && jsonPrimitive.isString()) {
                        Type generic = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        if (generic instanceof Class<?>) {
                            EnumOption<?> option = (EnumOption<?>) field.get(null);
                            Enum<?> found = null;
                            for (Enum<?> value : ((Class<Enum<?>>) generic).getEnumConstants()) {
                                if (value.name().toLowerCase(Locale.ROOT).equals(jsonPrimitive.getAsString())) {
                                    found = value;
                                    break;
                                }
                            }
                            if (found != null) {
                                ConfigStorage.setEnumRaw(option.getKey(), found);
                            }
                        }
                    }
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
                }else if (EnumOption.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType) {
                    Type generic = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (generic instanceof Class<?>) {
                        EnumOption<?> option = (EnumOption<?>) field.get(null);
                        config.addProperty(field.getName().toLowerCase(Locale.ROOT),
                                ConfigStorage.getEnumRaw(option.getKey(), (Class<Enum<?>>) generic)
                                        .name()
                                        .toLowerCase(Locale.ROOT)
                        );
                    }
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
