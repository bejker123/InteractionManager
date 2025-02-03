package com.bejker.interactionmanager;

import com.bejker.interactionmanager.InteractionManager;
import com.bejker.interactionmanager.config.Config;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class InteractionManagerClientTest {
    @BeforeAll
    static void bootstrapRegistries(){
        SharedConstants.isDevelopment = true;
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    void testOnInteractBlock(ItemStack stack, Block block,boolean is_cancelled){
        CallbackInfoReturnable<ActionResult> cir = new CallbackInfoReturnable<>("result",true,null);
        InteractionManager.onInteractBlock(stack, block,cir);
        Assertions.assertEquals(cir.isCancelled(), is_cancelled);
    }

    @Test
    void testOnInteractBlockShovel(){
        Block block = Blocks.GRASS_BLOCK;

        testOnInteractBlock(Items.WOODEN_SHOVEL.getDefaultStack(),block,false);
        testOnInteractBlock(Items.STONE_SHOVEL.getDefaultStack(),block,false);
        testOnInteractBlock(Items.IRON_SHOVEL.getDefaultStack(),block,false);
        testOnInteractBlock(Items.DIAMOND_SHOVEL.getDefaultStack(),block,false);
        testOnInteractBlock(Items.NETHERITE_SHOVEL.getDefaultStack(),block,false);

        Config.ALLOW_SHOVEL_CREATE_PATHS.setValue(false);

        testOnInteractBlock(Items.WOODEN_SHOVEL.getDefaultStack(),block,true);
        testOnInteractBlock(Items.STONE_SHOVEL.getDefaultStack(),block,true);
        testOnInteractBlock(Items.IRON_SHOVEL.getDefaultStack(),block,true);
        testOnInteractBlock(Items.DIAMOND_SHOVEL.getDefaultStack(),block,true);
        testOnInteractBlock(Items.NETHERITE_SHOVEL.getDefaultStack(),block,true);
    }

    @Test
    void testOnInteractBlockAxe(){
        Block block = Blocks.OAK_LOG;

        testOnInteractBlock(Items.WOODEN_AXE.getDefaultStack(),block,false);
        testOnInteractBlock(Items.STONE_AXE.getDefaultStack(),block,false);
        testOnInteractBlock(Items.IRON_AXE.getDefaultStack(),block,false);
        testOnInteractBlock(Items.DIAMOND_AXE.getDefaultStack(),block,false);
        testOnInteractBlock(Items.NETHERITE_AXE.getDefaultStack(),block,false);

        Config.ALLOW_AXE_STRIP_BLOCKS.setValue(false);

        testOnInteractBlock(Items.WOODEN_AXE.getDefaultStack(),block,true);
        testOnInteractBlock(Items.STONE_AXE.getDefaultStack(),block,true);
        testOnInteractBlock(Items.IRON_AXE.getDefaultStack(),block,true);
        testOnInteractBlock(Items.DIAMOND_AXE.getDefaultStack(),block,true);
        testOnInteractBlock(Items.NETHERITE_AXE.getDefaultStack(),block,true);
    }

    //void testOnAttackEntity(UUID player_uuid, Entity target,boolean is_cancelled){
    //    CallbackInfo cir = new CallbackInfo("result",true);
    //    InteractionManagerClient.onAttackEntity(player_uuid, target,cir);
    //    Assertions.assertEquals(cir.isCancelled(), is_cancelled);
    //}
    //@Test
    //void testOnAttackEntity(){
    //    UUID uuid = new UUID(4,20);
    //    WolfEntity entity = new WolfEntity(EntityType.WOLF,null);
    //    testOnAttackEntity(uuid,entity,false);

    //    Config.PET_ATTACK_MODE.setValue(Config.PetAttackMode.ONLY_OTHER);

    //    entity.setOwnerUuid(uuid);

    //    testOnAttackEntity(uuid,entity,true);
    //}
}
