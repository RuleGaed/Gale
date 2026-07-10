package org.galemc.gale.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class EntityPushGrid {

    private static final int CELL_BITS = 3;
    private final Long2ObjectOpenHashMap<ArrayList<Entity>> cells = new Long2ObjectOpenHashMap<>();

    public void register(Entity entity) {
        long key = cellKey(entity);
        ArrayList<Entity> cell = cells.get(key);
        if (cell == null) {
            cell = new ArrayList<>(4);
            cells.put(key, cell);
        }
        cell.add(entity);
    }

    public List<Entity> getEntities(AABB aabb, Predicate<Entity> predicate) {
        List<Entity> result = new ArrayList<>();
        int minCellX = Mth.floor(aabb.minX) >> CELL_BITS;
        int maxCellX = Mth.floor(aabb.maxX) >> CELL_BITS;
        int minCellZ = Mth.floor(aabb.minZ) >> CELL_BITS;
        int maxCellZ = Mth.floor(aabb.maxZ) >> CELL_BITS;
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cz = minCellZ; cz <= maxCellZ; cz++) {
                long key = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
                ArrayList<Entity> cell = cells.get(key);
                if (cell != null) {
                    for (int i = 0; i < cell.size(); i++) {
                        Entity entity = cell.get(i);
                        if (predicate.test(entity)) {
                            result.add(entity);
                        }
                    }
                }
            }
        }
        return result;
    }

    public void clear() {
        this.cells.clear();
    }

    private static long cellKey(Entity entity) {
        return ((long) (entity.getBlockX() >> CELL_BITS) << 32) | (entity.getBlockZ() >> CELL_BITS & 0xFFFFFFFFL);
    }

}
