package com.bejker.interactionmanager.client;

import com.bejker.interactionmanager.client.config.InteractionManagerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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
        if(!InteractionManagerConfig.ALLOW_SHOVEL_CREATE_PATHS.getValue()
                &&stack.getItem() instanceof ShovelItem){
            if(ShovelItem.PATH_STATES.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }

        if(!InteractionManagerConfig.ALLOW_AXE_STRIP_BLOCKS.getValue()
                &&stack.getItem() instanceof AxeItem){
            if(AxeItem.STRIPPED_BLOCKS.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }

        if(!InteractionManagerConfig.ALLOW_USE_FIREWORK_ON_BLOCK.getValue()
                &&stack.getItem() instanceof FireworkRocketItem){
                cir.setReturnValue(ActionResult.PASS);
        }
    }

    public static void onAttackEntity(UUID player_uuid, Entity target, CallbackInfo ci) {
        boolean is_hostile = (target instanceof HostileEntity) || Monster.class.isAssignableFrom(target.getClass());
        if(!InteractionManagerConfig.ALLOW_ATTACKING_HOSTILE_ENTITIES.getValue()&&
                is_hostile){
           ci.cancel();
           return;
        }
        if(!InteractionManagerConfig.ALLOW_ATTACKING_PASSIVE_ENTITIES.getValue()&&
                !is_hostile){
            ci.cancel();
            return;
        }
        if(target instanceof TameableEntity pet){
            InteractionManagerConfig.PetAttackMode petAttackMode = InteractionManagerConfig.PET_ATTACK_MODE.getValue();
            if(petAttackMode == InteractionManagerConfig.PetAttackMode.NONE){
                ci.cancel();
            }
            boolean is_owner = Objects.equals(pet.getOwnerUuid(), player_uuid);
            if(is_owner && petAttackMode == InteractionManagerConfig.PetAttackMode.ONLY_OTHER){
                ci.cancel();
            }
            if(pet.isTamed() && petAttackMode == InteractionManagerConfig.PetAttackMode.NOT_TAMED){
                ci.cancel();
            }
            return;
        }
        if(!InteractionManagerConfig.ALLOW_ATTACKING_VILLAGERS.getValue() && target instanceof VillagerEntity){
            ci.cancel();
        }
    }

    @Override
    public void onInitializeClient() {
        InteractionManagerConfig.loadConfig();
        InteractionManagerConfig.initRuntimeOptions();
    }
}
