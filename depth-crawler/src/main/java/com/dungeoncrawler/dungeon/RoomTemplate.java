package com.dungeoncrawler.dungeon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class RoomTemplate {

    private final String name;
    private final String category; // NORMAL / BOSS / REST
    private final int width;
    private final int depth;
    private final int spawnX;
    private final int spawnZ;
    private final int exitX;
    private final int exitZ;
    private final List<String> blocks;

    public RoomTemplate(String name, String category, int width, int depth,
                        int spawnX, int spawnZ, int exitX, int exitZ, List<String> blocks) {
        this.name = name;
        this.category = category;
        this.width = width;
        this.depth = depth;
        this.spawnX = spawnX;
        this.spawnZ = spawnZ;
        this.exitX = exitX;
        this.exitZ = exitZ;
        this.blocks = blocks == null ? new ArrayList<>() : blocks;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnZ() {
        return spawnZ;
    }

    public int getExitX() {
        return exitX;
    }

    public int getExitZ() {
        return exitZ;
    }

    public List<String> getBlocks() {
        return blocks;
    }

    /**
     * テンプレートを生成。
     */
    public void generate(World world, Location origin) {
        int ox = origin.getBlockX();
        int oy = origin.getBlockY();
        int oz = origin.getBlockZ();
        for (String block : blocks) {
            String[] parts = block.split(",");
            if (parts.length != 4) {
                continue;
            }
            try {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                Material mat = Material.matchMaterial(parts[3]);
                if (mat != null) {
                    world.getBlockAt(ox + x, oy + y, oz + z).setType(mat);
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static RoomTemplate capture(String name, String category, World world, Location pos1, Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        List<String> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Material mat = world.getBlockAt(x, y, z).getType();
                    if (mat == Material.AIR || mat == Material.CAVE_AIR) {
                        continue;
                    }
                    blocks.add((x - minX) + "," + (y - minY) + "," + (z - minZ) + "," + mat.name());
                }
            }
        }

        int spawnX = width / 2;
        int spawnZ = 2;
        int exitX = width / 2;
        int exitZ = depth - 2;

        return new RoomTemplate(name, category, width, depth, spawnX, spawnZ, exitX, exitZ, blocks);
    }
}
