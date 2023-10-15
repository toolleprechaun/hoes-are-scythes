package net.taylor.hoesarescythes;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.taylor.hoesarescythes.util.ModTags;

public class ClearGrass {

    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, direction) -> {
            if (world.isClient) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            boolean didWork = false;

            BlockState targetedBlock = world.getBlockState(blockPos);

            if (stack.getItem() instanceof HoeItem &&
                    (targetedBlock.isIn(ModTags.Blocks.SCYTHE_BLOCKS) ||
                            targetedBlock.isIn(ModTags.Blocks.SCYTHE_BLOCKS_DROPPABLE) ||
                            targetedBlock.isIn(BlockTags.CROPS))) {

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

                boolean isTargetingCrop = targetedBlock.isIn(BlockTags.CROPS);
                boolean isTargetingFullyGrownCrop = false;

                if (isTargetingCrop) {
                    for (Property<?> property : targetedBlock.getProperties()) {
                        if (property instanceof IntProperty ageProperty && property.getName().equals("age")) {
                            int age = targetedBlock.get(ageProperty);
                            int maxAge = ageProperty.getValues().stream().max(Integer::compareTo).get();
                            if (age == maxAge) {
                                isTargetingFullyGrownCrop = true;
                            }
                        }
                    }
                }

                for(int x = -radius; x <= radius; x++) {
                    for(int z = -radius; z <= radius; z++) {
                        BlockPos targetPos = blockPos.add(x, 0, z);
                        BlockState targetState = world.getBlockState(targetPos);

                        if (isTargetingCrop && targetState.isIn(BlockTags.CROPS)) {
                            if (isTargetingFullyGrownCrop) {
                                for (Property<?> property : targetState.getProperties()) {
                                    if (property instanceof IntProperty ageProperty && property.getName().equals("age")) {
                                        int age = targetState.get(ageProperty);
                                        int maxAge = ageProperty.getValues().stream().max(Integer::compareTo).get();

                                        if (age == maxAge) {
                                            world.breakBlock(targetPos, true);
                                            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                                            didWork = true;
                                        }
                                    }
                                }
                            } else {
                                world.breakBlock(targetPos, true);
                                stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                                didWork = true;
                            }
                        } else if (!isTargetingCrop && targetState.isIn(ModTags.Blocks.SCYTHE_BLOCKS)) {
                            world.breakBlock(targetPos, false);
                            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                            didWork = true;
                        } else if (!isTargetingCrop && targetState.isIn(ModTags.Blocks.SCYTHE_BLOCKS_DROPPABLE)) {
                            world.breakBlock(targetPos, true);
                            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                            didWork = true;
                        }
                    }
                }

                if (didWork) {
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }
}

