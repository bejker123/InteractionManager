package com.bejker.interactionmanager;

import com.bejker.interactionmanager.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
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
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class Interactions {
    public static int should_place_off_hand_in = -1;
    private static int SHOULD_PLACE_OFF_HAND_IN_DEFAULT = 2; // in ticks

    private static BlockHitResult cached_blockhr = null;

    public static void onInteractBlock(ItemStack stack, Block block, CallbackInfoReturnable<ActionResult> cir) {
        if(block instanceof DoorBlock){
            if(!Config.ALLOW_OPENING_DOORS.getValue()){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }
        if(block instanceof BlockWithEntity){
            if(!Config.ALLOW_OPENING_BLOCKS.getValue()){
                cir.setReturnValue(ActionResult.PASS);
            }
            return;
        }
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

        HashSet<Block> deniedBlockInteractions = Config.DENIED_ITEM_INTERACTIONS.get(stack.getItem());
        if(deniedBlockInteractions != null){
            // We use minecraft:air to cancel all block interactions for a given item
            if(deniedBlockInteractions.contains(block)||deniedBlockInteractions.contains(Blocks.AIR)){
                cir.setReturnValue(ActionResult.PASS);
            }
        }

        // We use minecraft:air to cancel all item interactions for a given set of blocks
        HashSet<Block> deniedBlockInteractionsForAllBlocks = Config.DENIED_ITEM_INTERACTIONS.get(Items.AIR);
        if(deniedBlockInteractionsForAllBlocks != null) {
            if (deniedBlockInteractionsForAllBlocks.contains(block) || deniedBlockInteractionsForAllBlocks.contains(Blocks.AIR)) {
                cir.setReturnValue(ActionResult.PASS);
            }
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
        if(Config.ENABLE_ENTITY_DENY_LIST.getValue() && Config.DENIED_ENTITIES.contains(target.getType())){
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
            if(pet.getOwner() != null){
                boolean is_owner = Objects.equals(pet.getOwner().getUuid(), player_uuid);
                if(is_owner && petAttackMode == Config.PetAttackMode.ONLY_OTHER){
                    return true;
                }
                return pet.isTamed() && petAttackMode == Config.PetAttackMode.NOT_TAMED;
            }
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
        if(Config.ENABLE_BLOCK_DENY_LIST.getValue() && Config.DENIED_BLOCKS.contains(block)){
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

        if(!Config.ALLOW_DRINKING_POTIONS.getValue()&&item instanceof PotionItem){
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if(Config.DENIED_ITEMS.contains(item)){
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
        if(Config.LOCK_HOT_BAR.getValue()&&(actionType.equals(SlotActionType.THROW) &&(0 <= slotId - 36&&slotId - 36 <= 9))){
            ci.cancel();
            return;
        }
        if(Config.LOCK_HOT_BAR.getValue()&&((actionType.equals(SlotActionType.PICKUP)||actionType.equals(SlotActionType.PICKUP_ALL) || actionType.equals(SlotActionType.SWAP)) &&(0 <= slotId - 36&&slotId - 36 <= 9))){
            ci.cancel();
            return;
        }
    }

    public static void onBreakBlock(ClientPlayerInteractionManager manager, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null){
            return;
        }
        if(!Config.REPLACE_BLOCKS.getValue()){
           return;
        }
        ItemStack offHandStack = player.getStackInHand(Hand.OFF_HAND);
        if(offHandStack == null||offHandStack.isEmpty()) {
            return;
        }
        if(offHandStack.getItem() instanceof BlockItem blockItem){
            BlockState blockState = player.getWorld().getBlockState(pos);
            if(blockItem.getBlock().equals(blockState.getBlock())){
               should_place_off_hand_in = Config.SHOULD_REPLACE_WITH_SAME_BLOCK.getValue() ? SHOULD_PLACE_OFF_HAND_IN_DEFAULT : -1;
                if(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr){
                    cached_blockhr = bhr;
                }
            }else{
                should_place_off_hand_in = SHOULD_PLACE_OFF_HAND_IN_DEFAULT;
                if(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult bhr){
                    cached_blockhr = bhr;
                }
            }
        }
    }

    public static void onTick(ClientPlayerEntity player,CallbackInfo ci) {
        if(Interactions.should_place_off_hand_in >= 0){
            Interactions.should_place_off_hand_in -= 1;
            if(Interactions.should_place_off_hand_in != -1){
                return;
            }
            if(MinecraftClient.getInstance().interactionManager == null) {
                return;
            }
            if(cached_blockhr == null) {
                return;
            }
            //ActionResult result = MinecraftClient.getInstance().interactionManager.interactBlock(player, Hand.OFF_HAND, cached_blockhr);
            ItemStack itemStack = player.getStackInHand(Hand.OFF_HAND);
            int i = itemStack.getCount();
            ActionResult actionResult2 = MinecraftClient.getInstance().interactionManager.interactBlock(player, Hand.OFF_HAND, cached_blockhr);
            if (Config.ANIMATE_REPLACE_BLOCKS.getValue()&&actionResult2.isAccepted()) {
                if (actionResult2.isAccepted()) {
                    player.swingHand(Hand.OFF_HAND);
                    if (!itemStack.isEmpty() && (itemStack.getCount() != i || player.isCreative())) {
                        MinecraftClient.getInstance().gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.OFF_HAND);
                    }
                }

                return;
            }
            //player.swingHand(Hand.OFF_HAND);
            cached_blockhr = null;
        }
    }
}
