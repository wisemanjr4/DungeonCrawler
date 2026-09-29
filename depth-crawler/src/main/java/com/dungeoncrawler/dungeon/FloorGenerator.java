package com.dungeoncrawler.dungeon;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.maze.MazeGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

import java.util.HashSet;
import java.util.Set;

public class FloorGenerator {

    private final DepthCrawlerPlugin plugin;
    private final int grid;
    private final int inner;
    private final int cell; // inner + 1 (壁1ブロック)
    private final int baseY;

    public FloorGenerator(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.grid = plugin.getDungeonConfig().getGridSize();
        this.inner = plugin.getDungeonConfig().getRoomInnerSize();
        this.cell = inner + 1;
        this.baseY = plugin.getDungeonConfig().getBaseY();
    }

    public int getCell() {
        return cell;
    }

    public int getGrid() {
        return grid;
    }

    public int getBaseY() {
        return baseY;
    }

    public int getFloorSize() {
        return grid * cell + 1;
    }

    /**
     * フロア全体の座標変換。floor0: Z=0, floor1: Z=121, ...
     */
    public int floorStartZ(int floor) {
        return floor * plugin.getDungeonConfig().getFloorSpacing();
    }

    /**
     * 指定フロアを生成する。
     */
    public void generate(World world, int floor, boolean restFloor, boolean bossFloor) {
        generate(world, floor, restFloor, bossFloor, false);
    }

    /**
     * 指定フロアを生成する。treasure=true で宝箱出現率が2倍になる（TREASURE修飾子）。
     */
    public void generate(World world, int floor, boolean restFloor, boolean bossFloor, boolean treasure) {
        int size = getFloorSize();
        int originZ = floorStartZ(floor);

        // 迷路生成
        MazeGenerator maze = new MazeGenerator(grid);
        maze.generate(0, 0);

        // 基礎床（Y=63 石レンガ）
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Block base = world.getBlockAt(x, baseY, originZ + z);
                base.setType(Material.STONE_BRICKS, false);
            }
        }

        // 部屋の床（Y=64）
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                fillRoomFloor(world, gx, gz, originZ, maze);
            }
        }

        // 壁（Y=65〜67）
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                buildRoomWalls(world, gx, gz, originZ, maze);
            }
        }

        // 天井（Y=68）
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                buildCeiling(world, gx, gz, originZ);
            }
        }

        // 外壁（全周）
        buildOuterWall(world, size, originZ);

        // 装飾
        decorate(world, originZ, maze, restFloor, bossFloor, treasure);
    }

    private void fillRoomFloor(World world, int gx, int gz, int originZ, MazeGenerator maze) {
        int xStart = gx * cell;
        int zStart = gz * cell;
        Material floorMaterial = randomFloorMaterial();

        for (int x = 1; x <= inner; x++) {
            for (int z = 1; z <= inner; z++) {
                Block b = world.getBlockAt(xStart + x, baseY + 1, originZ + zStart + z);
                b.setType(floorMaterial, false);
            }
        }
    }

    private Material randomFloorMaterial() {
        double r = Math.random();
        if (r < 0.4) {
            return Material.POLISHED_ANDESITE;
        } else if (r < 0.75) {
            return Material.STONE_BRICKS;
        }
        return Material.TERRACOTTA;
    }

    private void buildRoomWalls(World world, int gx, int gz, int originZ, MazeGenerator maze) {
        int xStart = gx * cell;
        int zStart = gz * cell;

        // 左壁（x = xStart）
        for (int z = 1; z <= inner; z++) {
            if (gx > 0 && !maze.hasWallBetween(gx, gz, gx - 1, gz)) {
                // 通路3ブロック
                if (z >= inner / 2 && z <= inner / 2 + 2) {
                    continue;
                }
            }
            buildWallColumn(world, xStart, originZ + zStart + z);
        }
        // 右壁（x = xStart + inner + 1）
        for (int z = 1; z <= inner; z++) {
            if (gx < grid - 1 && !maze.hasWallBetween(gx, gz, gx + 1, gz)) {
                if (z >= inner / 2 && z <= inner / 2 + 2) {
                    continue;
                }
            }
            buildWallColumn(world, xStart + inner + 1, originZ + zStart + z);
        }
        // 手前壁（z = zStart）
        for (int x = 1; x <= inner; x++) {
            if (gz > 0 && !maze.hasWallBetween(gx, gz, gx, gz - 1)) {
                if (x >= inner / 2 && x <= inner / 2 + 2) {
                    continue;
                }
            }
            buildWallRow(world, xStart + x, originZ + zStart);
        }
        // 奥壁（z = zStart + inner + 1）
        for (int x = 1; x <= inner; x++) {
            if (gz < grid - 1 && !maze.hasWallBetween(gx, gz, gx, gz + 1)) {
                if (x >= inner / 2 && x <= inner / 2 + 2) {
                    continue;
                }
            }
            buildWallRow(world, xStart + x, originZ + zStart + inner + 1);
        }
    }

    private void buildWallColumn(World world, int x, int z) {
        for (int y = baseY + 1; y <= baseY + 3; y++) {
            world.getBlockAt(x, y, z).setType(Material.STONE_BRICKS, false);
        }
    }

    private void buildWallRow(World world, int x, int z) {
        for (int y = baseY + 1; y <= baseY + 3; y++) {
            world.getBlockAt(x, y, z).setType(Material.STONE_BRICKS, false);
        }
    }

    private void buildCeiling(World world, int gx, int gz, int originZ) {
        int xStart = gx * cell;
        int zStart = gz * cell;
        int ceilY = baseY + 4;

        for (int x = 1; x <= inner; x++) {
            for (int z = 1; z <= inner; z++) {
                world.getBlockAt(xStart + x, ceilY, originZ + zStart + z).setType(Material.STONE_BRICKS, false);
            }
        }
        // 四隅のグロウストーン
        world.getBlockAt(xStart + 1, ceilY, originZ + zStart + 1).setType(Material.GLOWSTONE, false);
        world.getBlockAt(xStart + inner, ceilY, originZ + zStart + 1).setType(Material.GLOWSTONE, false);
        world.getBlockAt(xStart + 1, ceilY, originZ + zStart + inner).setType(Material.GLOWSTONE, false);
        world.getBlockAt(xStart + inner, ceilY, originZ + zStart + inner).setType(Material.GLOWSTONE, false);
    }

    private void buildOuterWall(World world, int size, int originZ) {
        for (int i = 0; i < size; i++) {
            for (int y = baseY + 1; y <= baseY + 4; y++) {
                world.getBlockAt(0, y, originZ + i).setType(Material.STONE_BRICKS, false);
                world.getBlockAt(size - 1, y, originZ + i).setType(Material.STONE_BRICKS, false);
                world.getBlockAt(i, y, originZ).setType(Material.STONE_BRICKS, false);
                world.getBlockAt(i, y, originZ + size - 1).setType(Material.STONE_BRICKS, false);
            }
        }
    }

    private void decorate(World world, int originZ, MazeGenerator maze, boolean restFloor, boolean bossFloor, boolean treasure) {
        // 宝箱配置（TREASURE修飾子で2倍）。ランダムな部屋に設置。
        double chestChance = treasure ? 0.30 : 0.15;
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                if (gx == grid - 1 && gz == grid - 1) {
                    continue; // 出口部屋には置かない
                }
                if (Math.random() < chestChance) {
                    int cx = gx * cell + inner / 2 + 1;
                    int cz = gz * cell + inner / 2 + 1;
                    world.getBlockAt(cx, baseY + 1, originZ + cz).setType(Material.CHEST, false);
                }
            }
        }

        // 30%の部屋の中央に柱 + 松明
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                if (Math.random() < 0.30 && !(gx == grid - 1 && gz == grid - 1)) {
                    int cx = gx * cell + inner / 2 + 1;
                    int cz = gz * cell + inner / 2 + 1;
                    // 柱はチェストと重なる場合スキップ
                    if (world.getBlockAt(cx, baseY + 1, originZ + cz).getType() == Material.CHEST) {
                        continue;
                    }
                    for (int y = baseY + 1; y <= baseY + 3; y++) {
                        world.getBlockAt(cx, y, originZ + cz).setType(Material.STONE_BRICKS, false);
                    }
                    world.getBlockAt(cx, baseY + 4, originZ + cz).setType(Material.TORCH, false);
                }
            }
        }

        // 出口部屋（grid[9][9]）
        int exitX = (grid - 1) * cell + inner / 2 + 1;
        int exitZ = (grid - 1) * cell + inner / 2 + 1;
        int ox = exitX;
        int oz = exitZ;

        if (bossFloor) {
            // 出口にレッドストーンブロック（BOSS）
            world.getBlockAt(ox, baseY + 1, originZ + oz).setType(Material.REDSTONE_BLOCK, false);
        } else if (restFloor) {
            // 休息F: 出口にビーコン
            world.getBlockAt(ox, baseY + 1, originZ + oz).setType(Material.EMERALD_BLOCK, false);
            // 石ボタン（東壁に取り付け）
            int wallX = (grid - 1) * cell + inner + 1;
            org.bukkit.block.Block button = world.getBlockAt(wallX - 1, baseY + 2, originZ + oz);
            button.setType(Material.STONE_BUTTON, false);
            org.bukkit.block.data.type.Switch switchData = (org.bukkit.block.data.type.Switch) button.getBlockData();
            switchData.setFacing(org.bukkit.block.BlockFace.WEST); // 東壁(x+1)に取り付く＝西を向く
            button.setBlockData(switchData, false);
        } else {
            // 通常: エメラルド（次フロア）とゴールド（帰還）
            world.getBlockAt(ox, baseY + 1, originZ + oz).setType(Material.EMERALD_BLOCK, false);
            world.getBlockAt(ox + 2, baseY + 1, originZ + oz).setType(Material.GOLD_BLOCK, false);
        }
    }

    public Location getSpawnLocation(World world, int floor) {
        // 迷路の起点である部屋(0,0)の中央（壁の列に湧かないようにする）
        double center = inner / 2 + 1 + 0.5;
        return new Location(world, center, baseY + 2, floorStartZ(floor) + center);
    }

    public Location getExitLocation(World world, int floor) {
        int exitX = (grid - 1) * cell + inner / 2 + 1;
        int exitZ = (grid - 1) * cell + inner / 2 + 1;
        return new Location(world, exitX + 0.5, baseY + 2, floorStartZ(floor) + exitZ + 0.5);
    }
}
