package com.bejker.interactionmanager;

import com.bejker.interactionmanager.config.Config;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
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
        double d = player.getMovement().horizontalLengthSquared();
        double e = (double)player.getMovementSpeed() * (double)2.5F;
        if (d < MathHelper.square(e) && player.getStackInHand(Hand.MAIN_HAND).isIn(ItemTags.SWORDS)) {
            bl4 = true;
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
}
