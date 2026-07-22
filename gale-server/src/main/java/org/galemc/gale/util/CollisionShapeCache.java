package org.galemc.gale.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CollisionShapeCache {

    private static VoxelShape[] cache;
    private static boolean[] uncacheable;
    private static volatile boolean precomputed;
    private static final ThreadLocal<Boolean> precomputing = ThreadLocal.withInitial(() -> false);

    private static void precompute() {
        if (precomputed) {
            return;
        }
        synchronized (CollisionShapeCache.class) {
            if (precomputed) {
                return;
            }
            precomputing.set(true);
            try {
                int size = Block.BLOCK_STATE_REGISTRY.size();
                cache = new VoxelShape[size];
                uncacheable = new boolean[size];
                for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
                    int index = state.indexInRegistry;
                    try {
                        cache[index] = state.getCollisionShape(null, null, CollisionContext.empty());
                    } catch (Exception ignored) {
                        uncacheable[index] = true;
                    }
                }
                precomputed = true;
            } finally {
                precomputing.set(false);
            }
        }
    }

    public static VoxelShape getCachedCollisionShape(BlockState state) {
        if (precomputing.get()) {
            return null;
        }
        if (!precomputed) {
            precompute();
        }
        int index = state.indexInRegistry;
        if (!uncacheable[index]) {
            return cache[index];
        }
        return null;
    }
}
