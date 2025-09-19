package net.taylor.hoesarescythes;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.taylor.hoesarescythes.logic.RadiusResolver;
import net.taylor.hoesarescythes.logic.ScythePredicate;

public final class ClearGrass {

    private ClearGrass() {}

    public static void register() {
        AttackBlockCallback.EVENT.register(ClearGrass::onAttackBlock);
    }

    private static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, net.minecraft.util.math.Direction face) {
        if (world.isClient) return ActionResult.PASS;

        ItemStack tool = player.getStackInHand(hand);
        if (player.isSneaking() || !(tool.getItem() instanceof HoeItem)) return ActionResult.PASS;

        int radius = RadiusResolver.getRadius(tool);
        if (radius <= 0) return ActionResult.PASS;

        BlockState state = world.getBlockState(pos);
        if (!isValidInitialTarget(state)) return ActionResult.PASS;

        boolean didWork = breakBlocksInRadius(player, (ServerWorld) world, hand, pos, state, tool, radius);
        return didWork ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    private static boolean breakBlocksInRadius(PlayerEntity player, ServerWorld world, Hand hand,
                                               BlockPos origin, BlockState initial, ItemStack tool, int radius) {
        boolean didWork = false;

        final boolean targetingCrop = initial.isIn(BlockTags.CROPS);
        final boolean targetingNetherWart = initial.isOf(Blocks.NETHER_WART);
        final boolean targetingFullyGrown = isFullyGrownCrop(initial);

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                mutable.set(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                didWork = tryBreakBlock(
                        player, world, hand, mutable,
                        targetingCrop, targetingNetherWart, targetingFullyGrown,
                        tool
                ) || didWork;
            }
        }
        return didWork;
    }

    private static boolean tryBreakBlock(PlayerEntity player, ServerWorld world, Hand hand, BlockPos pos,
                                         boolean targetingCrop, boolean targetingNetherWart, boolean targetingFullyGrown,
                                         ItemStack tool) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return false;
        if (!player.getAbilities().allowModifyWorld) return false;
        if (world.getServer().isSpawnProtected(world, pos, player)) return false;
        if (!shouldBreakBlock(targetingCrop, targetingFullyGrown, targetingNetherWart, state)) return false;

        boolean broke = world.breakBlock(pos, !player.getAbilities().creativeMode);
        if (!broke) return false;

        if (!player.getAbilities().creativeMode && tool.isDamageable()) {
            tool.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return true;
    }

    private static boolean isValidInitialTarget(BlockState state) {
        return state.isIn(BlockTags.CROPS)
                || state.isOf(Blocks.NETHER_WART)
                || ScythePredicate.isScythable(state);
    }

    private static boolean shouldBreakBlock(boolean targetingCrop, boolean targetingFullyGrown,
                                            boolean targetingNetherWart, BlockState state) {
        if (targetingCrop && state.isIn(BlockTags.CROPS)) {
            return !targetingFullyGrown || isFullyGrownCrop(state);
        }
        if (targetingNetherWart && state.isOf(Blocks.NETHER_WART)) {
            return !targetingFullyGrown || isFullyGrownCrop(state);
        }
        return !targetingCrop && !targetingNetherWart && ScythePredicate.isScythable(state);
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