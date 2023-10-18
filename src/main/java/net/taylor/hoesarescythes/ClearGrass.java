package net.taylor.hoesarescythes;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.taylor.hoesarescythes.util.ModTags;

public class ClearGrass {

    public static void register() {
        AttackBlockCallback.EVENT.register(ClearGrass::handleAttackBlock);
    }

    private static ActionResult handleAttackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {

        if (world.isClient) {
            return ActionResult.PASS;
        }

        ItemStack stack = playerEntity.getStackInHand(hand);
        BlockState targetedBlock = world.getBlockState(blockPos);

        if (playerEntity.isSneaking() || !(stack.getItem() instanceof HoeItem)) {
            return ActionResult.PASS;
        }

        boolean didWork = breakBlocksInRadius(playerEntity, world, hand, blockPos, targetedBlock, stack);
        return didWork ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    private static boolean breakBlocksInRadius(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, BlockState targetedBlock, ItemStack stack) {
        boolean didWork = false;
        int radius = getRadiusBasedOnMaterial(((HoeItem) stack.getItem()).getMaterial());
        boolean isTargetingCrop = targetedBlock.isIn(BlockTags.CROPS);
        boolean isTargetingFullyGrownCrop = isFullyGrownCrop(targetedBlock);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                didWork |= tryBreakBlock(playerEntity, world, hand, blockPos.add(x, 0, z), isTargetingCrop, isTargetingFullyGrownCrop, stack);
            }
        }

        return didWork;
    }

    private static boolean tryBreakBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos targetPos, boolean isTargetingCrop, boolean isTargetingFullyGrownCrop, ItemStack stack) {
        BlockState targetState = world.getBlockState(targetPos);

        if (!shouldBreakBlock(isTargetingCrop, isTargetingFullyGrownCrop, targetState)) {
            return false;
        }

        if (playerEntity instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.interactionManager.tryBreakBlock(targetPos);
        } else {
            world.breakBlock(targetPos, true);
        }

        stack.damage(1, playerEntity, p -> p.sendToolBreakStatus(hand));
        return true;
    }


    private static int getRadiusBasedOnMaterial(ToolMaterial material) {
        if (material == ToolMaterials.WOOD || material == ToolMaterials.STONE) {
            return 1;
        } else if (material == ToolMaterials.IRON) {
            return 2;
        } else if (material == ToolMaterials.DIAMOND) {
            return 3;
        } else {
            return 4;
        }
    }


    private static boolean isFullyGrownCrop(BlockState blockState) {
        for (Property<?> property : blockState.getProperties()) {
            if (!(property instanceof IntProperty ageProperty)) continue;
            if (!property.getName().equals("age")) continue;

            int age = blockState.get(ageProperty);
            int maxAge = ageProperty.getValues().stream().max(Integer::compareTo).orElse(0);
            if (age == maxAge) {
                return true;
            }
        }
        return false;
    }


    private static boolean shouldBreakBlock(boolean isTargetingCrop, boolean isTargetingFullyGrownCrop, BlockState targetState) {
        if (isTargetingCrop && targetState.isIn(BlockTags.CROPS)) {
            return !isTargetingFullyGrownCrop || isFullyGrownCrop(targetState);
        } else return !isTargetingCrop && targetState.isIn(ModTags.Blocks.SCYTHE_BLOCKS);
    }

}

