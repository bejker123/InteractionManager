package com.bejker.interactionmanager.client.config.option;

import java.util.HashMap;
import java.util.Map;

public class ConfigStorage {
    private static final Map<String,Boolean> BOOLEAN_OPTIONS = new HashMap<>();
    private static final Map<String,Enum<?>> ENUM_OPTIONS = new HashMap<>();

    public static void setBoolean(String key,boolean value){
        BOOLEAN_OPTIONS.put(key,value);
    }

    public static boolean getBoolean(String key){
        return BOOLEAN_OPTIONS.get(key);
    }

    public static void toggleBoolean(String key) {
        setBoolean(key,!getBoolean(key));
    }

    public static <E extends Enum<E>> E getEnum(String key, Class<E> type_class) {
        return (E) ENUM_OPTIONS.get(key);
    }

    public static Enum<?> getEnumRaw(String key,Class<Enum<?>> type_class) {
        return ENUM_OPTIONS.get(key);
    }

    public static <E extends Enum<E>> void setEnum(String key,E value) {
        ENUM_OPTIONS.put(key,value);
    }

    public static void setEnumRaw(String key,Enum<?> value) {
        ENUM_OPTIONS.put(key,value);
    }

    public static <E extends Enum<E>> E cycleEnumValue(String key,Class<E> type_class,int amount){
        E[] values = type_class.getEnumConstants();
        E currentValue = getEnum(key, type_class);
        E newValue = values[(currentValue.ordinal() + amount) % values.length];
        setEnum(key, newValue);
        return newValue;
    }
}
