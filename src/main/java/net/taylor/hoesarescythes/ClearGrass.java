package net.taylor.hoesarescythes;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.taylor.hoesarescythes.util.ModTags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearGrass {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearGrass.class);

    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, direction) -> {
            if (world.isClient) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            boolean didWork = false;

            BlockState targetedBlock = world.getBlockState(blockPos);

            if (stack.getItem() instanceof HoeItem &&
                    (targetedBlock.isIn(ModTags.Blocks.SCYTHE_BLOCKS) || targetedBlock.isIn(ModTags.Blocks.SCYTHE_BLOCKS_DROPPABLE))) {

                LOGGER.info("Conditions met.");

                int radius;
                ToolMaterial material = ((HoeItem) stack.getItem()).getMaterial();

                if (material == ToolMaterials.WOOD || material == ToolMaterials.STONE) {
                    radius = 1;
                } else if (material == ToolMaterials.IRON) {
                    radius = 2;
                } else if (material == ToolMaterials.DIAMOND) {
                    radius = 3;
                } else {
                    radius = 4;
                }

                for(int x = -radius; x <= radius; x++) {
                    for(int z = -radius; z <= radius; z++) {
                        BlockPos targetPos = blockPos.add(x, 0, z);
                        BlockState targetState = world.getBlockState(targetPos);

                        if (targetState.isIn(ModTags.Blocks.SCYTHE_BLOCKS)) {
                            world.breakBlock(targetPos, false);
                            didWork = true;
                        } else if (targetState.isIn(ModTags.Blocks.SCYTHE_BLOCKS_DROPPABLE)) {
                            world.breakBlock(targetPos, true);
                            didWork = true;
                        }
                    }
                }

                if (didWork) {
                    stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                    return ActionResult.SUCCESS;
                }
            } else {
                LOGGER.warn("Conditions not met.");
            }

            return ActionResult.PASS;
        });
    }
}

