package com.bejker.interactionmanager.client.config;

import com.bejker.interactionmanager.InteractionManager;
import com.bejker.interactionmanager.client.config.option.*;
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
import java.util.List;
import java.util.Locale;

import static com.bejker.interactionmanager.client.InteractionManagerClient.CLIENT_LOGGER;

public class InteractionManagerConfig {
    private static Path config_path;
    public static final BooleanOption ALLOW_SHOVEL_CREATE_PATHS = new BooleanOption("allow_shovel_create_paths");
    public static final BooleanOption ALLOW_AXE_STRIP_BLOCKS = new BooleanOption("allow_axe_strip_blocks");
    public static final BooleanOption ALLOW_USE_FIREWORK_ON_BLOCK = new BooleanOption("allow_use_firework_on_block");
    public static final BooleanOption ALLOW_ATTACKING_HOSTILE_ENTITIES = new BooleanOption("allow_attacking_hostile_entities");
    public static final BooleanOption ALLOW_ATTACKING_PASSIVE_ENTITIES = new BooleanOption("allow_attacking_passive_entities");
    public static final BooleanOption ALLOW_ATTACKING_VILLAGERS = new BooleanOption("allow_attacking_villagers");

    public static final EnumOption<PetAttackMode> PET_ATTACK_MODE = new EnumOption<PetAttackMode>("pet_attack_mode",PetAttackMode.ALL);

    @IRuntimeInternalOnlyOption
    public static final BooleanOption IS_MODMENU_INSTALLED = new BooleanOption("mod_menu_installed",false);

    @IFileOnlyOption
    public static final EnumOption<ShouldAddInteractionsButton> SHOULD_ADD_INTERACTIONS_BUTTON  = new EnumOption<ShouldAddInteractionsButton>("should_add_interactions_button",ShouldAddInteractionsButton.ALWAYS);

    public enum PetAttackMode{
        ALL,
        ONLY_OTHER,
        NOT_TAMED,
        NONE
    }

    public enum ShouldAddInteractionsButton{
        ALWAYS,
        ONLY_IF_MOD_MENU_IS_NOT_INSTALLED,
        NEVER
    }

    private static void setupConfigFile(){
        if(config_path != null){
            return;
        }
        config_path = FabricLoader.getInstance().getConfigDir().resolve(InteractionManager.MOD_ID + ".json");
    }

    public static SimpleOption<?>[] asOptions() {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();
        for (Field field : InteractionManagerConfig.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(IRuntimeInternalOnlyOption.class)){
                continue;
            }
            if(field.isAnnotationPresent(IFileOnlyOption.class)){
                continue;
            }
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
            boolean found_invalid = false;
            for(Field field: InteractionManagerConfig.class.getDeclaredFields()){
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
                                ConfigStorage.setEnumRaw(option.getKey(), found);
                            } else{
                                CLIENT_LOGGER.error("Invalid option in Interaction Manager config; Path: \"{}\": \"{}\":\"{}\"; Restoring saved: \"{}\"; Valid values: {}",config_path ,option.getKey(),jsonPrimitive.getAsString(),option.getValue().name().toLowerCase(Locale.ROOT),valid_values);
                                found_invalid = true;
                            }
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
            for (Field field : InteractionManagerConfig.class.getDeclaredFields()) {
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

    public static void restoreDefaults() {
        try{
            for (Field field : InteractionManagerConfig.class.getDeclaredFields()){
                if(field.isAnnotationPresent(IRuntimeInternalOnlyOption.class) || field.isAnnotationPresent(IFileOnlyOption.class)){
                    continue;
                }
                if(BooleanOption.class.isAssignableFrom(field.getType())){
                    BooleanOption option = (BooleanOption) field.get(null);
                    ConfigStorage.setBoolean(option.getKey(),option.getDefaultValue());
                }else if(EnumOption.class.isAssignableFrom(field.getType())){
                    EnumOption<?> option = (EnumOption<?>) field.get(null);
                    ConfigStorage.setEnumRaw(option.getKey(),option.getDefaultValue());
                }
            }
        }catch(IllegalAccessException e){
            System.err.println("Couldn't restore Interaction Manager config to defaults.");
            e.printStackTrace();
        }
    }

    public static boolean areOptionValuesSetToDefault() {
        try{
            for (Field field : InteractionManagerConfig.class.getDeclaredFields()){
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
        IS_MODMENU_INSTALLED.setValue(FabricLoader.getInstance().getModContainer("modmenu").isPresent());
    }

}
