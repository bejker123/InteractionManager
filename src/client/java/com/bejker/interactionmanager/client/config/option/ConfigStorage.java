package com.bejker.interactionmanager.client.config.option;

import java.util.HashMap;
import java.util.Map;

public class ConfigStorage {
    private static final Map<String,Boolean> BOOLEAN_OPTIONS = new HashMap<>();

    public static void setBoolean(String key,boolean value){
        BOOLEAN_OPTIONS.put(key,value);
    }

    public static boolean getBoolean(String key){
        return BOOLEAN_OPTIONS.get(key);
    }

    public static void toggleBoolean(String key) {
        setBoolean(key,!getBoolean(key));
    }
}
