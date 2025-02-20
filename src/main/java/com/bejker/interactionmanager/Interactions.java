package com.bejker.interactionmanager;

import com.bejker.interactionmanager.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

public class Interactions {
    public static void onInteractBlock(ItemStack stack, Block block, CallbackInfoReturnable<ActionResult> cir) {
        if(!Config.ALLOW_PLACING_BLOCKS.getValue()&&stack.getItem() instanceof BlockItem){
            cir.setReturnValue(ActionResult.PASS);
            return;
        }
        if(!Config.ALLOW_SHOVEL_CREATE_PATHS.getValue()
                &&stack.getItem() instanceof ShovelItem){
            if(ShovelItem.PATH_STATES.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
        }

        if(!Config.ALLOW_AXE_STRIP_BLOCKS.getValue()
                &&stack.getItem() instanceof AxeItem){
            if(AxeItem.STRIPPED_BLOCKS.get(block) != null){
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
        }

        if(!Config.ALLOW_USE_FIREWORK_ON_BLOCK.getValue()
                &&stack.getItem() instanceof FireworkRocketItem){
                cir.setReturnValue(ActionResult.PASS);
                return;
        }

        if(!Config.ALLOW_OPENING_BLOCKS.getValue()
           &&block instanceof BlockWithEntity){
            cir.setReturnValue(ActionResult.PASS);
            return;
        }
    }

    private static boolean protectFromSweepingEdge(UUID player_uuid,Entity target){
        World world = MinecraftClient.getInstance().world;
        if(world == null){
            return false;
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null){
            return false;
        }

        boolean ret = false;
        for (LivingEntity newTarget : world.getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0))) {
            if (newTarget != player
                    && newTarget != target
                    && !player.isTeammate(newTarget)
                    && (!(newTarget instanceof ArmorStandEntity) || !((ArmorStandEntity)newTarget).isMarker())
                    && player.squaredDistanceTo(newTarget) < 9.0) {
                if(isProtected(player_uuid,newTarget)){
                    RenderProtected.markAsProtected(newTarget);
                    ret = true;
                }
            }
        }
        return ret;
    }

    private static boolean isProtected(UUID player_uuid,Entity target){
        if(Config.ENABLE_ENTITY_BLACKLIST.getValue() && Config.BLACKLISTED_ENTITIES.contains(target.getType())){
            return true;
        }

        if(!Config.ALLOW_ATTACKING_PLAYERS.getValue() && target instanceof PlayerEntity){
            return true;
        }

        boolean is_hostile = (target instanceof HostileEntity) || Monster.class.isAssignableFrom(target.getClass());
        if(!Config.ALLOW_ATTACKING_HOSTILE_ENTITIES.getValue()&&
                is_hostile){
            return true;
        }
        if(!Config.ALLOW_ATTACKING_PASSIVE_ENTITIES.getValue() &&
                !is_hostile &&
                target instanceof PassiveEntity){
            return true;
        }
        if(target instanceof TameableEntity pet){
            Config.PetAttackMode petAttackMode =Config.PET_ATTACK_MODE.getValue();
            if(petAttackMode == Config.PetAttackMode.NONE){
                return true;
            }
            boolean is_owner = Objects.equals(pet.getOwnerUuid(), player_uuid);
            if(is_owner && petAttackMode == Config.PetAttackMode.ONLY_OTHER){
                return true;
            }
            return pet.isTamed() && petAttackMode == Config.PetAttackMode.NOT_TAMED;
        }
        if(!Config.ALLOW_ATTACKING_VILLAGERS.getValue() && target instanceof VillagerEntity){
            return true;
        }
        return !Config.ALLOW_ATTACKING_VEHICLES.getValue() && target instanceof VehicleEntity;
    }

    public static void onAttackEntity(UUID player_uuid, Entity target, CallbackInfo ci) {
        if(MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null){
            return;
        }
        if(isProtected(player_uuid,target)){
            RenderProtected.markAsProtected(target);
            ci.cancel();
            return;
        }
        if(!Config.PROTECT_FROM_SWEEPING_EDGE.getValue()){
            return;
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        if(wouldDealSweepingEdgeDamage(player,target)) {
            if(protectFromSweepingEdge(player_uuid,target)){
                RenderProtected.markAsUnprotected(target);
                ci.cancel();
            }
        }
    }

    //Sourced from PlayerEntity.attack
    private static boolean wouldDealSweepingEdgeDamage(PlayerEntity player, Entity target){
        float h = player.getAttackCooldownProgress(0.5F);
        boolean bl4 = false;
        boolean bl = h > 0.9F;
        boolean bl2;
        bl2 = player.isSprinting() && bl;
        boolean bl3 = bl
                && player.fallDistance > 0.0F
                && !player.isOnGround()
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !player.hasVehicle()
                && target instanceof LivingEntity
                && !player.isSprinting();
        double d = (double)(player.horizontalSpeed - player.prevHorizontalSpeed);
        if (bl && !bl3 && !bl2 && player.isOnGround() && d < (double)player.getMovementSpeed()) {
            ItemStack itemStack2 = player.getStackInHand(Hand.MAIN_HAND);
            if (itemStack2.getItem() instanceof SwordItem) {
                bl4 = true;
            }
        }
        return bl4;
    }

    public static void restrictBlockBreaking(Block block, CallbackInfoReturnable<Boolean> cir) {
        if(!Config.ALLOW_BREAKING_BLOCKS.getValue()){
            cir.setReturnValue(true);
        }
        if(Config.ENABLE_BLOCK_BLACKLIST.getValue() && Config.BLACKLISTED_BLOCKS.contains(block)){
            cir.setReturnValue(true);
        }
    }

    public static void onUseItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
        if (!Config.ALLOW_EATING.getValue()&&foodComponent != null) {
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }

        Item item = itemStack.getItem();

        if(!Config.ALLOW_USING_ENDER_PEARL.getValue()&&item instanceof EnderPearlItem){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if(!Config.ALLOW_USING_ENDER_EYE.getValue()&&item instanceof EnderEyeItem){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if(!Config.ALLOW_USING_BOWS.getValue()&&item instanceof BowItem){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if(!Config.ALLOW_USING_CROSSBOWS.getValue()&&item instanceof CrossbowItem){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if(!Config.ALLOW_DRINKING_POTIONS.getValue()&&item instanceof PotionItem){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
    }

    public static void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(!Config.ALLOW_ENTITY_INTERACTION.getValue()){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if(!Config.ALLOW_VILLAGER_TRADING.getValue()&&entity instanceof VillagerEntity){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
    }

    public static void onSlotClick(int syncId, int slotId, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if(!Config.ALLOW_DROPPING_ITEMS.getValue()&&(actionType.equals(SlotActionType.THROW) || slotId < 0)){
            ci.cancel();
            return;
        }
        if(!Config.ALLOW_DROPPING_HOT_BAR_ITEMS.getValue()&&(actionType.equals(SlotActionType.THROW) &&(0 <= slotId - 36&&slotId - 36 <= 9))){
            ci.cancel();
            return;
        }
    }
}
