package com.bejker.interactionmanager.util;

import com.bejker.interactionmanager.InteractionManager;
import net.minecraft.util.ActionResult;

import java.lang.reflect.Method;

public class Util {
    public static String translationKeyOf(String type,String id){
        return type+"."+ InteractionManager.MOD_ID + "." + id;
    }

    public static String getTooltipTranslationKey(String type,String key){
        return translationKeyOf(type,key) + ".tooltip";
    }

    public static boolean doesOverrideMethod(Class<?> clazz,String methodName) throws NoSuchMethodException {
        return doesOverrideMethod(clazz,methodName,clazz.getSuperclass());
    }

    public static boolean doesOverrideMethod(Class<?> clazz,String methodName,Class<?> superClass) throws NoSuchMethodException {
        if(superClass == Object.class||clazz == superClass){
            return false;
        }
        Method method = Util.getMethod(clazz,methodName);
        Method method1 = Util.getMethod(superClass,methodName);
        return !method.getDeclaringClass().equals(method1.getDeclaringClass());
    }

    public static Method getMethod(Class<?> clazz, String methodName)
            throws NoSuchMethodException {
        if (clazz == null)
            throw new NoSuchMethodException(methodName);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName))
                return method;
        }
        try {
            return getMethod(clazz.getSuperclass(), methodName);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("No method named " + methodName
                    + " in " + clazz + " (or super classes)");
        }
    }
}
