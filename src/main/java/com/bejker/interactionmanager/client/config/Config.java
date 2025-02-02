package com.bejker.interactionmanager.client.config;

import com.bejker.interactionmanager.client.config.option.*;
import net.minecraft.block.Block;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

// Use this class to access and set config options.
// For saving and loading config use ConfigManager
// For internal storage use OptionStorage
public class Config {
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
    public static final EnumOption<ShouldAddInteractionsButton> SHOULD_ADD_INTERACTIONS_BUTTON  = new EnumOption<ShouldAddInteractionsButton>("should_add_interactions_button",ShouldAddInteractionsButton.ONLY_IF_MOD_MENU_IS_NOT_INSTALLED);

    @IFileOnlyOption
    public static final BooleanOption ALLOW_BREAKING_BLOCKS = new BooleanOption("allow_breaking_blocks");

    public static final ArrayList<Block> BLACKLISTED_BLOCKS = new ArrayList<>();

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

    public static SimpleOption<?>[] asOptions() {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();
        for (Field field : Config.class.getDeclaredFields()) {
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

}
