package com.bejker.interactionmanager.config;

import com.bejker.interactionmanager.config.option.BooleanOption;
import com.bejker.interactionmanager.config.option.EnumOption;
import com.bejker.interactionmanager.config.option.interfaces.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.EntityType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

// Use this class to access and set config options.
// For saving and loading config use ConfigManager
// For internal storage use OptionStorage
public class Config {
    //Option names are intentionally verbose for clarity reasons

    @IItemOption
    public static final BooleanOption ALLOW_SHOVEL_CREATE_PATHS = new BooleanOption("allow_shovel_create_paths");

    @IItemOption
    public static final BooleanOption ALLOW_AXE_STRIP_BLOCKS = new BooleanOption("allow_axe_strip_blocks");

    @IEntityOption
    public static final BooleanOption ALLOW_ATTACKING_PLAYERS = new BooleanOption("allow_attacking_players");

    @IEntityOption
    public static final BooleanOption ALLOW_ATTACKING_HOSTILE_ENTITIES = new BooleanOption("allow_attacking_hostile_entities");

    @IEntityOption
    public static final BooleanOption ALLOW_ATTACKING_PASSIVE_ENTITIES = new BooleanOption("allow_attacking_passive_entities");

    @IEntityOption
    public static final BooleanOption ALLOW_ATTACKING_VILLAGERS = new BooleanOption("allow_attacking_villagers");

    @IEntityOption
    public static final BooleanOption ALLOW_ATTACKING_VEHICLES = new BooleanOption("allow_attacking_vehicles");

    @IEntityOption
    public static final EnumOption<PetAttackMode> PET_ATTACK_MODE = new EnumOption<PetAttackMode>("pet_attack_mode",PetAttackMode.ALL);

    @IEntityOption
    public static final BooleanOption PROTECT_FROM_SWEEPING_EDGE = new BooleanOption("protect_from_sweeping_edge");

    @IEntityOption
    public static final BooleanOption ALLOW_ENTITY_INTERACTION = new BooleanOption("allow_entity_interaction");

    @IEntityOption
    public static final BooleanOption ALLOW_VILLAGER_TRADING = new BooleanOption("allow_villager_trading");

    @IRenderOption
    public static final BooleanOption RENDER_PROTECTED_ENTITIES = new BooleanOption("render_protected_entities");

    @IRenderOption
    public static final BooleanOption RENDER_PROTECTED_BLOCKS = new BooleanOption("render_protected_blocks");

    @IRuntimeInternalOnlyOption
    public static final BooleanOption IS_MODMENU_INSTALLED = new BooleanOption("mod_menu_installed",false);

    @IRenderOption
    public static final EnumOption<ShouldAddInteractionsButton> SHOULD_ADD_INTERACTIONS_BUTTON  = new EnumOption<ShouldAddInteractionsButton>("should_add_interactions_button",ShouldAddInteractionsButton.ONLY_IF_MOD_MENU_IS_NOT_INSTALLED);

    @IBlockOption
    public static final BooleanOption ALLOW_BREAKING_BLOCKS = new BooleanOption("allow_breaking_blocks");

    @IBlockOption
    public static final BooleanOption ALLOW_PLACING_BLOCKS = new BooleanOption("allow_placing_blocks");

    @IBlockOption
    public static final BooleanOption ALLOW_OPENING_BLOCKS = new BooleanOption("allow_opening_blocks");

    @IBlockOption
    public static final BooleanOption ALLOW_OPENING_DOORS = new BooleanOption("allow_opening_doors");

    @IBlockOption
    public static final BooleanOption REPLACE_BLOCKS = new BooleanOption("replace_blocks",false);

    @IBlockOption
    public static final BooleanOption SHOULD_REPLACE_WITH_SAME_BLOCK = new BooleanOption("should_replace_with_same_block",false);

    @IBlockOption
    public static final BooleanOption ENABLE_BLOCK_DENY_LIST = new BooleanOption("enable_block_deny_list",true,"enabled","disabled");

    public static final Set<Block> DENIED_BLOCKS = new HashSet<>();

    public static final Set<EntityType<?>> DENIED_ENTITIES = new HashSet<>();

    public static final HashMap<Item,HashSet<Block>> DENIED_ITEM_INTERACTIONS = new HashMap<>();

    public static final HashSet<Item> DENIED_ITEMS = new HashSet<>();

    @IEntityOption
    public static final BooleanOption ENABLE_ENTITY_DENY_LIST = new BooleanOption("enable_entity_deny_list",true,"enabled","disabled");

    @IRenderOption
    public static final BooleanOption RENDER_ITEMS_IN_BLOCK_DENY_LIST = new BooleanOption("render_items_in_block_deny_list");

    @IItemOption
    public static final BooleanOption ALLOW_EATING = new BooleanOption("allow_eating");

    @IItemOption
    public static final BooleanOption ALLOW_DRINKING_POTIONS = new BooleanOption("allow_drinking_potions");

    @IItemOption
    public static final BooleanOption ALLOW_DROPPING_ITEMS = new BooleanOption("allow_dropping_items");

    @IItemOption
    public static final BooleanOption LOCK_HOT_BAR = new BooleanOption("lock_hot_bar",false);

    @IRenderOption
    public static final BooleanOption ANIMATE_REPLACE_BLOCKS = new BooleanOption("animate_replace_blocks");

    @IRenderOption
    public static final BooleanOption RENDER_DENIED_BLOCK_PLACEMENT = new BooleanOption("render_denied_block_placement");

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

    public static SimpleOption<?>[] asOptions(Class<? extends Annotation> target) {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();
        for (Field field : Config.class.getDeclaredFields()) {
            if(field.isAnnotationPresent(IRuntimeInternalOnlyOption.class)){
                continue;
            }
            if(field.isAnnotationPresent(IFileOnlyOption.class)){
                continue;
            }
            if(target != null){
                if(!field.isAnnotationPresent(target)){
                    continue;
                }
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
