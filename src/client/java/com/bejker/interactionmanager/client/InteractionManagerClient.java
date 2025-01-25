package com.bejker.interactionmanager.client;

import com.bejker.interactionmanager.config.InteractionManagerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.bejker.interactionmanager.InteractionManager.MOD_ID;

public class InteractionManagerClient implements ClientModInitializer {

    public static Logger CLIENT_LOGGER = LoggerFactory.getLogger(MOD_ID+":client");

    public static void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        if(!InteractionManagerConfig.getInstance().ALLOW_SHOVEL_USE_ON_BLOCK
                &&stack.getItem() instanceof ShovelItem){
            disallowShovelInteractBlock(hitResult,cir);
        }
    }

    private static void disallowShovelInteractBlock(BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(MinecraftClient.getInstance().world == null){
            return;
        }
        Block block = MinecraftClient.getInstance().world.getBlockState(hitResult.getBlockPos()).getBlock();
       if(ShovelItem.PATH_STATES.get(block) != null){
           cir.setReturnValue(ActionResult.PASS);
       }
    }

    @Override
    public void onInitializeClient() {
        InteractionManagerConfig.getInstance().loadConfig();
    }
}
