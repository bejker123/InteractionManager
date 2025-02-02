package com.bejker.interactionmanager.client;

import com.bejker.interactionmanager.client.config.Config;
import com.bejker.interactionmanager.client.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

import static com.bejker.interactionmanager.InteractionManager.MOD_ID;

public class InteractionManagerClient implements ClientModInitializer {

    public static Logger CLIENT_LOGGER = LoggerFactory.getLogger(MOD_ID+":client");

    public static void onInteractBlock(ItemStack stack, Block block, CallbackInfoReturnable<ActionResult> cir) {
        if(!Config.ALLOW_SHOVEL_CREATE_PATHS.getValue()
                &&stack.getItem() instanceof ShovelItem){
            if(ShovelItem.PATH_STATES.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }

        if(!Config.ALLOW_AXE_STRIP_BLOCKS.getValue()
                &&stack.getItem() instanceof AxeItem){
            if(AxeItem.STRIPPED_BLOCKS.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }

        if(!Config.ALLOW_USE_FIREWORK_ON_BLOCK.getValue()
                &&stack.getItem() instanceof FireworkRocketItem){
                cir.setReturnValue(ActionResult.PASS);
        }
    }

    public static void onAttackEntity(UUID player_uuid, Entity target, CallbackInfo ci) {
        boolean is_hostile = (target instanceof HostileEntity) || Monster.class.isAssignableFrom(target.getClass());
        if(!Config.ALLOW_ATTACKING_HOSTILE_ENTITIES.getValue()&&
                is_hostile){
           ci.cancel();
           return;
        }
        if(!Config.ALLOW_ATTACKING_PASSIVE_ENTITIES.getValue()&&
                !is_hostile){
            ci.cancel();
            return;
        }
        if(target instanceof TameableEntity pet){
            Config.PetAttackMode petAttackMode = Config.PET_ATTACK_MODE.getValue();
            if(petAttackMode == Config.PetAttackMode.NONE){
                ci.cancel();
            }
            boolean is_owner = Objects.equals(pet.getOwnerUuid(), player_uuid);
            if(is_owner && petAttackMode == Config.PetAttackMode.ONLY_OTHER){
                ci.cancel();
            }
            if(pet.isTamed() && petAttackMode == Config.PetAttackMode.NOT_TAMED){
                ci.cancel();
            }
            return;
        }
        if(!Config.ALLOW_ATTACKING_VILLAGERS.getValue() && target instanceof VillagerEntity){
            ci.cancel();
        }
    }

    public static void restrictBlockBreaking(Block block, CallbackInfoReturnable<Boolean> cir) {
        if(!Config.ALLOW_BREAKING_BLOCKS.getValue()){
            cir.setReturnValue(true);
        }
    }


    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();
        ConfigManager.initRuntimeOptions();
    }
}
