package com.bejker.interactionmanager.config;

import com.bejker.interactionmanager.config.option.*;
import net.minecraft.block.Block;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.EntityType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// Use this class to access and set config options.
// For saving and loading config use ConfigManager
// For internal storage use OptionStorage
public class Config {
    //Option names are intentionally verbose for clarity reasons

    public static final BooleanOption ALLOW_SHOVEL_CREATE_PATHS = new BooleanOption("allow_shovel_create_paths");
    public static final BooleanOption ALLOW_AXE_STRIP_BLOCKS = new BooleanOption("allow_axe_strip_blocks");
    public static final BooleanOption ALLOW_USE_FIREWORK_ON_BLOCK = new BooleanOption("allow_use_firework_on_block");
    public static final BooleanOption ALLOW_ATTACKING_PLAYERS = new BooleanOption("allow_attacking_players");
    public static final BooleanOption ALLOW_ATTACKING_HOSTILE_ENTITIES = new BooleanOption("allow_attacking_hostile_entities");
    public static final BooleanOption ALLOW_ATTACKING_PASSIVE_ENTITIES = new BooleanOption("allow_attacking_passive_entities");
    public static final BooleanOption ALLOW_ATTACKING_VILLAGERS = new BooleanOption("allow_attacking_villagers");
    public static final BooleanOption ALLOW_ATTACKING_VEHICLES = new BooleanOption("allow_attacking_vehicles");

    public static final EnumOption<PetAttackMode> PET_ATTACK_MODE = new EnumOption<PetAttackMode>("pet_attack_mode",PetAttackMode.ALL);

    @IRuntimeInternalOnlyOption
    public static final BooleanOption IS_MODMENU_INSTALLED = new BooleanOption("mod_menu_installed",false);

    @IFileOnlyOption
    public static final EnumOption<ShouldAddInteractionsButton> SHOULD_ADD_INTERACTIONS_BUTTON  = new EnumOption<ShouldAddInteractionsButton>("should_add_interactions_button",ShouldAddInteractionsButton.ONLY_IF_MOD_MENU_IS_NOT_INSTALLED);

    public static final BooleanOption ALLOW_BREAKING_BLOCKS = new BooleanOption("allow_breaking_blocks");

    public static final BooleanOption ENABLE_BLOCK_BLACKLIST = new BooleanOption("enable_block_blacklist",true,"enabled","disabled");

    public static final Set<Block> BLACKLISTED_BLOCKS = new HashSet<>();

    public static final Set<EntityType<?>> BLACKLISTED_ENTITIES = new HashSet<>();

    public static final BooleanOption ENABLE_ENTITY_BLACKLIST = new BooleanOption("enable_entity_blacklist",true,"enabled","disabled");

    @IFileOnlyOption
    public static final BooleanOption RENDER_ITEMS_IN_BLOCK_BLACKLIST = new BooleanOption("render_items_in_block_blacklist");

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
        return options.toArray(SimpleOption[]::new);
    }

}
