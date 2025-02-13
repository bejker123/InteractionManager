package com.bejker.interactionmanager.config;

import com.bejker.interactionmanager.InteractionManager;
import com.bejker.interactionmanager.config.option.*;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.bejker.interactionmanager.InteractionManager.CLIENT_LOGGER;


// Use this class to save, load, and init runtime config
// To access and set config options use Config.
// For internal storage use OptionStorage
public class ConfigManager {

    private static Path config_path;
    public static void loadConfig(){
        setupConfigFile();

        if (!Files.exists(config_path)) {
            saveConfig();
            return;
        }
        try{
            BufferedReader reader = Files.newBufferedReader(config_path);
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            boolean found_invalid = false;
            for(Field field: Config.class.getDeclaredFields()){
                if(field.isAnnotationPresent(IRuntimeInternalOnlyOption.class)){
                    continue;
                }
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
                            List<String> valid_values = new ArrayList<>();
                            for (Enum<?> value : ((Class<Enum<?>>) generic).getEnumConstants()) {
                                String value_name = value.name().toLowerCase(Locale.ROOT);
                                valid_values.add("\""+value_name+"\"");
                                if (value_name.equals(jsonPrimitive.getAsString())) {
                                    found = value;
                                    break;
                                }
                            }
                            if (found != null) {
                                OptionStorage.setEnumRaw(option.getKey(), found);
                            } else{
                                CLIENT_LOGGER.error("Invalid option in Interaction Manager config; Path: \"{}\": \"{}\":\"{}\"; Restoring saved: \"{}\"; Valid values: {}",config_path ,option.getKey(),jsonPrimitive.getAsString(),option.getValue().name().toLowerCase(Locale.ROOT),valid_values);
                                found_invalid = true;
                            }
                        }
                    }
                }else if(Set.class.isAssignableFrom(field.getType())){
                    if(field.getName().equals("BLACKLISTED_BLOCKS")){
                        JsonArray jsonArray = json.getAsJsonArray(field.getName()
                                .toLowerCase(Locale.ROOT));
                        if(jsonArray == null||jsonArray.isEmpty()){
                            continue;
                        }
                        for(JsonElement element : jsonArray) {
                            Block block = Registries.BLOCK.get(Identifier.of(element.getAsString()));
                            Config.BLACKLISTED_BLOCKS.add(block);
                        }
                    }else if(field.getName().equals("BLACKLISTED_ENTITIES")){
                        JsonArray jsonArray = json.getAsJsonArray(field.getName()
                                .toLowerCase(Locale.ROOT));
                        if(jsonArray == null||jsonArray.isEmpty()){
                            continue;
                        }
                        for(JsonElement element : jsonArray) {
                            EntityType<?> entityType = Registries.ENTITY_TYPE.get(Identifier.of(element.getAsString()));
                            Config.BLACKLISTED_ENTITIES.add(entityType);
                        }
                    }
                }
            }
            if(found_invalid){
                saveConfig();
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
            for (Field field : Config.class.getDeclaredFields()) {
                if(field.isAnnotationPresent(IRuntimeInternalOnlyOption.class)){
                    continue;
                }
                String field_name = field.getName().toLowerCase(Locale.ROOT);
                if (BooleanOption.class.isAssignableFrom(field.getType())) {
                    BooleanOption option = (BooleanOption) field.get(null) ;
                    config.addProperty(field_name, option.getValue());
                }else if (EnumOption.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType) {
                    Type generic = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (generic instanceof Class<?>) {
                        EnumOption<?> option = (EnumOption<?>) field.get(null);
                        config.addProperty(field.getName().toLowerCase(Locale.ROOT),
                                OptionStorage.getEnumRaw(option.getKey(), (Class<Enum<?>>) generic)
                                        .name()
                                        .toLowerCase(Locale.ROOT)
                        );
                    }
                }else if(Set.class.isAssignableFrom(field.getType())){
                    if(field.getName().equals("BLACKLISTED_BLOCKS")){
                        JsonArray array = new JsonArray();
                        for(var block : Config.BLACKLISTED_BLOCKS){
                            array.add(Registries.BLOCK.getId(block).toString());
                        }
                        config.add(field_name,array);
                    } else if(field.getName().equals("BLACKLISTED_ENTITIES")){
                        JsonArray array = new JsonArray();
                        for(var entityType : Config.BLACKLISTED_ENTITIES){
                            array.add(Registries.ENTITY_TYPE.getId(entityType).toString());
                        }
                        config.add(field_name,array);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        String string = InteractionManager.GSON.toJson(config);

        try {
            BufferedWriter writer = Files.newBufferedWriter(config_path);
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            System.err.println("Couldn't load Interaction Manager config, using defaults.");
            e.printStackTrace();
        }
    }

    public static void restoreDefaults() {
        try{
            for (Field field : Config.class.getDeclaredFields()){
                if(field.isAnnotationPresent(IRuntimeInternalOnlyOption.class) || field.isAnnotationPresent(IFileOnlyOption.class)){
                    continue;
                }
                if(BooleanOption.class.isAssignableFrom(field.getType())){
                    BooleanOption option = (BooleanOption) field.get(null);
                    OptionStorage.setBoolean(option.getKey(),option.getDefaultValue());
                }else if(EnumOption.class.isAssignableFrom(field.getType())){
                    EnumOption<?> option = (EnumOption<?>) field.get(null);
                    OptionStorage.setEnumRaw(option.getKey(),option.getDefaultValue());
                }
            }
        }catch(IllegalAccessException e){
            System.err.println("Couldn't restore Interaction Manager config to defaults.");
            e.printStackTrace();
        }
    }

    public static boolean areOptionValuesSetToDefault() {
        try{
            for (Field field : Config.class.getDeclaredFields()){
                if(field.isAnnotationPresent(IRuntimeInternalOnlyOption.class) || field.isAnnotationPresent(IFileOnlyOption.class)){
                    continue;
                }
                if(BooleanOption.class.isAssignableFrom(field.getType())){
                    BooleanOption option = (BooleanOption) field.get(null);
                    if(option.getValue() != option.getDefaultValue()){
                        return false;
                    }
                }else if(EnumOption.class.isAssignableFrom(field.getType())){
                    EnumOption<?> option = (EnumOption<?>) field.get(null);
                    if(option.getValue() != option.getDefaultValue()){
                        return false;
                    }
                }
            }
        }catch(IllegalAccessException e){
            System.err.println("Couldn't check if Interaction Manager config is set to defaults.");
            e.printStackTrace();
        }
        return true;
    }

    public static void initRuntimeOptions(){
        Config.IS_MODMENU_INSTALLED.setValue(FabricLoader.getInstance().getModContainer("modmenu").isPresent());
    }
    private static void setupConfigFile(){
        if(config_path != null){
            return;
        }
        config_path = FabricLoader.getInstance().getConfigDir().resolve(InteractionManager.MOD_ID + ".json");
    }
}
