package com.dungeoncrawler.dungeon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 拠点・ダンジョン入口・ショップ・闘技場などの静的な建築物を生成する。
 */
public class DungeonBuilder {

    private final Map<UUID, Location[]> selection = new HashMap<>();

    public void buildSpawn(World world, Location center) {
        buildBox(world, center, 25, 25, Material.STONE_BRICKS, Material.POLISHED_ANDESITE);
    }

    public void buildDungeonEntrance(World world, Location center) {
        buildBox(world, center, 15, 15, Material.MOSSY_STONE_BRICKS, Material.COBBLESTONE);
        for (int y = center.getBlockY() + 1; y < center.getBlockY() + 5; y++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    world.getBlockAt(center.getBlockX() + dx, y, center.getBlockZ() + dz).setType(Material.AIR);
                }
            }
        }
    }

    public void buildShop(World world, Location center) {
        buildBox(world, center, 15, 15, Material.OAK_PLANKS, Material.SMOOTH_STONE);
    }

    public void buildArena(World world, Location center, int width, int depth) {
        int w = Math.max(4, Math.min(200, width));
        int d = Math.max(4, Math.min(200, depth));
        int x = center.getBlockX();
        int z = center.getBlockZ();
        int y = center.getBlockY() - 1;
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.getBlockAt(x + dx, y, z + dz).setType(Material.STONE);
            }
        }
    }

    public void buildWallsAround(World world, Location pos1, Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        int y = pos1.getBlockY();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    world.getBlockAt(x, y, z).setType(Material.STONE_BRICKS);
                    world.getBlockAt(x, y + 1, z).setType(Material.STONE_BRICKS);
                    world.getBlockAt(x, y + 2, z).setType(Material.STONE_BRICKS);
                }
            }
        }
    }

    private void buildBox(World world, Location center, int w, int d, Material floorMat, Material wallMat) {
        int x = center.getBlockX();
        int z = center.getBlockZ();
        int y = center.getBlockY();
        for (int dx = -w / 2; dx <= w / 2; dx++) {
            for (int dz = -d / 2; dz <= d / 2; dz++) {
                world.getBlockAt(x + dx, y - 1, z + dz).setType(floorMat);
            }
        }
        for (int dx = -w / 2; dx <= w / 2; dx++) {
            for (int dz = -d / 2; dz <= d / 2; dz++) {
                if (dx == -w / 2 || dx == w / 2 || dz == -d / 2 || dz == d / 2) {
                    for (int dy = 0; dy < 4; dy++) {
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(wallMat);
                    }
                }
            }
        }
    }

    public void setSelection(UUID uuid, int which, Location loc) {
        selection.computeIfAbsent(uuid, k -> new Location[2])[which] = loc;
    }

    public Location[] getSelection(UUID uuid) {
        return selection.getOrDefault(uuid, new Location[2]);
    }

    public void clearSelection(UUID uuid) {
        selection.remove(uuid);
    }
}
