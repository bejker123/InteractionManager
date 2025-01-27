package com.bejker.interactionmanager.client;

import com.bejker.interactionmanager.config.InteractionManagerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
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

import static com.bejker.interactionmanager.InteractionManager.MOD_ID;

public class InteractionManagerClient implements ClientModInitializer {

    public static Logger CLIENT_LOGGER = LoggerFactory.getLogger(MOD_ID+":client");

    public static void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        ClientWorld world = MinecraftClient.getInstance().world;
        if(world == null){
            return;
        }
        Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();

        if(!InteractionManagerConfig.getInstance().ALLOW_SHOVEL_CREATE_PATHS
                &&stack.getItem() instanceof ShovelItem){
            if(ShovelItem.PATH_STATES.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }

        if(!InteractionManagerConfig.getInstance().ALLOW_AXE_STRIP_BLOCKS
                &&stack.getItem() instanceof AxeItem){
            if(AxeItem.STRIPPED_BLOCKS.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }

        if(!InteractionManagerConfig.getInstance().ALLOW_USE_FIREWORK_ON_BLOCK
                &&stack.getItem() instanceof FireworkRocketItem){
                cir.setReturnValue(ActionResult.PASS);
            return;
        }
    }

    public static void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if(player == null || target == null){
            return;
        }
        boolean is_hostile = (target instanceof HostileEntity) || Monster.class.isAssignableFrom(target.getClass());
        if(!InteractionManagerConfig.getInstance().ALLOW_ATTACKING_HOSTILE_ENTITIES&&
                is_hostile){
           ci.cancel();
           return;
        }
        if(!InteractionManagerConfig.getInstance().ALLOW_ATTACKING_PASSIVE_ENTITIES&&
                !is_hostile){
            ci.cancel();
            return;
        }
    }

    @Override
    public void onInitializeClient() {
        InteractionManagerConfig.getInstance().loadConfig();
    }
}
