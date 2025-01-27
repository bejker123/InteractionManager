package com.bejker.interactionmanager.util;

import com.bejker.interactionmanager.InteractionManager;

public class Util {
    public static String translationKeyOf(String type,String id){
        return type+"."+ InteractionManager.MOD_ID + "." + id;
    }

    public static String getTooltipTranslationKey(String type,String key){
        return translationKeyOf(type,key) + ".tooltip";
    }
}
