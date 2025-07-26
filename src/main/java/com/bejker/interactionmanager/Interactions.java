package com.bejker.interactionmanager;

import com.bejker.interactionmanager.config.Config;
import com.bejker.interactionmanager.mixin.PlayerEntityMixin;
import com.bejker.interactionmanager.mixin.PlayerInteractionManagerMixin;
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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
<<<<<<< HEAD
import net.minecraft.util.math.MathHelper;
||||||| ea7fb85
=======
import net.minecraft.util.math.Direction;
>>>>>>> 1.21.1
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

/**
 * Class handling player-environment interactions.
 * Unless otherwise noted functions can be freely used as an API to determine if an action will complete successfully.
 */
@Environment(EnvType.CLIENT)
public class Interactions {
    public static int should_place_off_hand_in = -1;
    private static int SHOULD_PLACE_OFF_HAND_IN_DEFAULT = 2; // in ticks

    private static BlockHitResult cached_blockhr = null;

    /**
     * Handles block interactions. Specifically for a given item(item stack) on a given block.
     * Think right-clicking a block with a hoe/axe/shovel.
     * @param stack The item stack, the actual stack data isn't check, only the item class is relevant
     * @param block The block, relevant especially if a given block implements a right-click interaction
     * @param cir Functionally the return of the function
     * @see PlayerInteractionManagerMixin#onInteractBlock(net.minecraft.client.network.ClientPlayerEntity, net.minecraft.util.Hand, net.minecraft.util.hit.BlockHitResult, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable)
     */
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

    /**
     * An internal utility function to check if a given target should be protected from sweeping edge damage
     * @param player_uuid The attacking player's uuid, relevant if pets are protected, but should always be well-formed. If not the return will always be false
     * @param target The target entity, really just relevant for the entity type, if the entity isn't a pet(tamable entity)
     * @return true if a given entity should be protected from sweeping edge damage otherwise false
     */
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

    /**
     * An internal utility function to check if a given entity should be protected from being dealt damage.
     * @param player_uuid The attacking player's uuid
     * @param target The target entity, relevant only for the type unless the given entity is tamable. Then also the uuid and tamed status is.
     * @return true if a given entity should be protected from being dealt damage, otherwise false
     */
    private static boolean isProtected(UUID player_uuid,Entity target){
        boolean ret = isProtectedInternal(player_uuid,target);
        if(Config.INVERT_ENTITY_DENY_LIST.getValue()){
           ret = !ret;
        }
        return ret;
    }

    private static boolean isProtectedInternal(UUID player_uuid,Entity target){
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

    /**
     * A function to check the result of attacking an entity.
     * @param player_uuid The attacking player's uuid, mostly relevant if tamable entities(pets) are protected
     * @param target The attacked entity, mostly relevant for its type. UUID and tamed status relevant if the entity is tameable(a pet).
     * @param ci Functionally the return of the function. If cancelled the action should not be performed. As the entity is protected.
     *           Or is sweeping damage would be dealt to a protected entity the ci will also be cancelled.
     * @see PlayerInteractionManagerMixin#onAttackEntity(PlayerEntity, Entity, CallbackInfo)
     */
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

    /**
     * A function to check if a given block should be broken.
     * @param block The block, mostly relevant for its type.
     * @param cir Functionally the return of the function, if the value is true the block should not be broken.
     * @see PlayerInteractionManagerMixin#updateBlockBreakingProgress(BlockPos, Direction, CallbackInfoReturnable)
     */
    public static void restrictBlockBreaking(Block block, CallbackInfoReturnable<Boolean> cir) {
        restrictBlockBreakingInternal(block,cir);
        if(Config.INVERT_BLOCK_DENY_LIST.getValue()){
            Boolean value = cir.getReturnValue();
            if(value == null){
                value = false;
            }
            cir.setReturnValue(!value);
        }
    }

    private static void restrictBlockBreakingInternal(Block block,CallbackInfoReturnable<Boolean> cir){
        if(!Config.ALLOW_BREAKING_BLOCKS.getValue()){
            cir.setReturnValue(true);
        }
        if(Config.ENABLE_BLOCK_DENY_LIST.getValue() && Config.DENIED_BLOCKS.contains(block)){
            cir.setReturnValue(true);
        }
    }

    /**
     * A function to check if a given item interaction should be performed, only applicable to using the item while not targeting a block. Ie "in the air"
     * @param player The player performing the interaction
     * @param hand The hand used to perform the action
     * @param cir Functionally the return of the function. If the value is ActionResult.FAIL the interaction should not be performed.
     * @see PlayerInteractionManagerMixin#onInteractItem(PlayerEntity, Hand, CallbackInfoReturnable)
     */
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

    /**
     * A function to check if a given entity interaction should be performed.
     * @param player The player performing the interaction
     * @param entity The entity affected
     * @param hand The hand used to perform the action
     * @param cir Functionally the return of the function. If the value is ActionResult.FAIL the interaction should not be performed.
     * @see PlayerInteractionManagerMixin#onInteractEntity(PlayerEntity, Entity, Hand, CallbackInfoReturnable)
     * @see PlayerInteractionManagerMixin#interactEntityAtLocation(PlayerEntity, Entity, EntityHitResult, Hand, CallbackInfoReturnable)
     */
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

    /**
     * A function to check the result of a slot click.
     * @param syncId Not relevant
     * @param slotId ID(index) of the clicked slot
     * @param actionType Type of action performed (left-click/right-click)
     * @param player The player performing the action
     * @param ci Functionally the return of the function. If cancelled the action should not be performed.
     * @see PlayerInteractionManagerMixin#onSlotClick(int, int, int, net.minecraft.screen.slot.SlotActionType, net.minecraft.entity.player.PlayerEntity, org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
     */
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

    /**
     * A function to update block replacing functionality. Should not be called as an API function.
     * @param manager the instance of ClientPlayerInteractionManager, can be null
     * @param pos The position of the block being broken
     * @param cir Functionally the return of the function, if value is true the block should be broken.
     * @see PlayerInteractionManagerMixin#onBreakBlock(net.minecraft.util.math.BlockPos, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable)
     */
    public static void onBreakBlock(@Nullable ClientPlayerInteractionManager manager, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
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

    /**
     * A function called every ClientPlayerEntity.tick. Should not be called as an API function.
     * Mostly relevant for replacing blocks.
     * @param player The player ticking
     * @param ci Functionally the return of the function if cancelled the tick should not be performed.
     * @see PlayerEntityMixin#onTick(org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
     */
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
                    if (!itemStack.isEmpty() && (itemStack.getCount() != i || MinecraftClient.getInstance().interactionManager.hasCreativeInventory())) {
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
