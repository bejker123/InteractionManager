package com.bejker.interactionmanager.client.config.option;

import com.bejker.interactionmanager.InteractionManager;

public class Util {
    public static String translationKeyOf(String type,String id){
        return type+"."+ InteractionManager.MOD_ID + "." + id;
    }
}
