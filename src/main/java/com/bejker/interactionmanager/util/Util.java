package com.bejker.interactionmanager.util;

import com.bejker.interactionmanager.InteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;

public class Util {
    public static String translationKeyOf(String type,String id){
        return type+"."+ InteractionManager.MOD_ID + "." + id;
    }

    public static String getTooltipTranslationKey(String type,String key){
        return translationKeyOf(type,key) + ".tooltip";
    }

    private static final Class<?>[] USE_METHOD_PARAMETER_TYPES = {World.class, PlayerEntity.class, Hand.class};
    private static final Class<?> USE_METHOD_RETURN_TYPE = ActionResult.class;

    public static boolean doesOverrideUse(Class<?> clazz){
        if(!Item.class.isAssignableFrom(clazz)){
            return false;
        }
        for(var method : clazz.getDeclaredMethods()){
            if(Arrays.equals(method.getParameterTypes(), USE_METHOD_PARAMETER_TYPES) && method.getReturnType().equals(USE_METHOD_RETURN_TYPE)){
                return !method.getDeclaringClass().equals(Item.class);
            }
        }
        return false;
    }

    //public ActionResult useOnBlock(ItemUsageContext context) {
    private static final Class<?>[] USE_ON_BLOCK_METHOD_PARAMETER_TYPES = {ItemUsageContext.class};
    private static final Class<?> USE_ON_BLOCK_METHOD_RETURN_TYPE = ActionResult.class;

    public static boolean doesOverrideUseOnBlock(Class<?> clazz){
        if(!Item.class.isAssignableFrom(clazz)){
            return false;
        }
        //InteractionManager.LOGGER.info("{}",clazz.getName());
        for(var method : clazz.getDeclaredMethods()){
            if(Arrays.equals(method.getParameterTypes(), USE_ON_BLOCK_METHOD_PARAMETER_TYPES) && method.getReturnType().equals(USE_ON_BLOCK_METHOD_RETURN_TYPE)){
                //InteractionManager.LOGGER.info("FOUND: {}",method.getName());
                //InteractionManager.LOGGER.info("Declaring class: {}",method.getDeclaringClass());
                return !method.getDeclaringClass().equals(Item.class);
            }
        }
        return false;
    }

}
