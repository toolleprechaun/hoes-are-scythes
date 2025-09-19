// net/taylor/hoesarescythes/ClearGrass.java

package net.taylor.hoesarescythes;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
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
        AttackBlockCallback.EVENT.register(ClearGrass::onAttackBlock);
    }

    private static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction face) {
        if (world.isClient) return ActionResult.PASS;

        ItemStack tool = player.getStackInHand(hand);
        if (player.isSneaking() || !(tool.getItem() instanceof HoeItem)) return ActionResult.PASS;

        BlockState state = world.getBlockState(pos);
        if (!isValidInitialTarget(state)) return ActionResult.PASS;

        boolean didWork = breakBlocksInRadius(player, (ServerWorld) world, hand, pos, state, tool);
        return didWork ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    private static boolean breakBlocksInRadius(PlayerEntity player, ServerWorld world, Hand hand,
                                               BlockPos origin, BlockState initial, ItemStack tool) {
        boolean didWork = false;

        int radius = getRadiusForHoe(tool);
        boolean targetingCrop = initial.isIn(BlockTags.CROPS);
        boolean targetingNetherWart = initial.isOf(Blocks.NETHER_WART);
        boolean targetingFullyGrown = isFullyGrownCrop(initial);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos target = origin.add(dx, 0, dz);
                didWork |= tryBreakBlock(
                        player, world, hand, target,
                        targetingCrop, targetingNetherWart, targetingFullyGrown,
                        tool
                );
            }
        }
        return didWork;
    }

    private static boolean tryBreakBlock(PlayerEntity player, ServerWorld world, Hand hand, BlockPos pos,
                                         boolean targetingCrop, boolean targetingNetherWart, boolean targetingFullyGrown,
                                         ItemStack tool) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return false;
        if (!player.canModifyAt(world, pos)) return false;
        if (!shouldBreakBlock(targetingCrop, targetingFullyGrown, targetingNetherWart, state)) return false;

        boolean broke = world.breakBlock(pos, true);
        if (!broke) return false;

        // Damage tool (1 per block)
        tool.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        return true;
    }

    private static boolean isValidInitialTarget(BlockState state) {
        return state.isIn(BlockTags.CROPS)
                || state.isOf(Blocks.NETHER_WART)
                || state.isIn(ModTags.Blocks.SCYTHE_BLOCKS);
    }

    private static boolean shouldBreakBlock(boolean targetingCrop, boolean targetingFullyGrown,
                                            boolean targetingNetherWart, BlockState state) {
        if (targetingCrop && state.isIn(BlockTags.CROPS)) {
            return !targetingFullyGrown || isFullyGrownCrop(state);
        }
        if (targetingNetherWart && state.isOf(Blocks.NETHER_WART)) {
            return !targetingFullyGrown || isFullyGrownCrop(state);
        }
        return !targetingCrop && !targetingNetherWart && state.isIn(ModTags.Blocks.SCYTHE_BLOCKS);
    }

    private static int getRadiusForHoe(ItemStack stack) {
        if (stack.isOf(Items.WOODEN_HOE) || stack.isOf(Items.STONE_HOE)) return 1;
        if (stack.isOf(Items.IRON_HOE)) return 2;
        if (stack.isOf(Items.DIAMOND_HOE)) return 3;
        return 4; // Netherite and others
    }

    private static boolean isFullyGrownCrop(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntProperty age && property.getName().equals("age")) {
                int ageValue = state.get(age);
                int maxAge = age.getValues().stream().max(Integer::compareTo).orElse(0);
                return ageValue == maxAge;
            }
        }
        return false;
    }
}