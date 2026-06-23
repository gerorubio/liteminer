package com.iamkaf.liteminer.shapes;

import com.iamkaf.liteminer.tags.TagHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class FiveByFiveWalker implements Walker {
    private static @NotNull BlockHitResult raytrace(Level level, Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 rotation = player.getViewVector(1);
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 combined = eyePosition.add(rotation.x * reach, rotation.y * reach, rotation.z * reach);

        return level.clip(new ClipContext(eyePosition, combined, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    @Override
    public String toString() {
        return "5x5";
    }

    public HashSet<BlockPos> walk(Level level, Player player, BlockPos origin) {
        Direction direction = raytrace(level, player).getDirection().getOpposite();
        HashSet<BlockPos> potentialBrokenBlocks = new HashSet<>();

        potentialBrokenBlocks.add(origin);

        BlockState originState = level.getBlockState(origin);

        if (originState.is(Blocks.AIR)) {
            return new HashSet<>(0);
        }

        if (TagHelper.isExcludedBlock(originState)) {
            return potentialBrokenBlocks;
        }

        searchBlocks(player, level, origin, origin, potentialBrokenBlocks, originState.getBlock(), direction);

        return potentialBrokenBlocks;
    }

    private void searchBlocks(Player player, Level level, BlockPos myPos, BlockPos absoluteOrigin,
        HashSet<BlockPos> blocksToCollapse, Block originBlock, Direction direction) {

        if (!shouldMine(player, level, myPos)) return;

        int radius = 2;

        if (direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) {

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = myPos.offset(x, 0, z);
                    if (shouldMine(player, level, pos)) {
                        blocksToCollapse.add(pos);
                    }
                }
            }

        } else {

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {

                    BlockPos pos = myPos
                            .relative(direction.getCounterClockWise(), x)
                            .above(y);

                    if (shouldMine(player, level, pos)) {
                        blocksToCollapse.add(pos);
                    }
                }
            }
        }

        blocksToCollapse.add(myPos);
    }

}
