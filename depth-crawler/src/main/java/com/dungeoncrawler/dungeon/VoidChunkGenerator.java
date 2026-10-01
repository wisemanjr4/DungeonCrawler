package com.dungeoncrawler.dungeon;

import com.dungeoncrawler.maze.MazeGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ダンジョンワールド用ジェネレーター。自然地形は生成せず、迷路の構造（床・壁・天井）を
 * チャンク生成時にまとめて書き込む。1ブロックずつ setType するより桁違いに速い。
 * 各フロアの {@link FloorPlan} は、そのフロアのチャンクが初めて要求された時点で作られ、以後固定される。
 */
public class VoidChunkGenerator extends ChunkGenerator {

    private final int grid;
    private final int inner;
    private final int cell;
    private final int size;
    private final int spacing;
    private final int baseY;
    private final Map<Integer, FloorPlan> plans = new ConcurrentHashMap<>();

    public VoidChunkGenerator(int grid, int inner, int spacing, int baseY) {
        this.grid = grid;
        this.inner = inner;
        this.cell = inner + 1;
        this.size = grid * cell + 1;
        this.spacing = spacing;
        this.baseY = baseY;
    }

    public FloorPlan plan(int floor) {
        return plans.computeIfAbsent(floor, f -> new FloorPlan(grid));
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0.5, baseY + 2, 0.5);
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData data) {
        for (int lx = 0; lx < 16; lx++) {
            int x = chunkX * 16 + lx;
            if (x < 0 || x >= size) {
                continue;
            }
            for (int lz = 0; lz < 16; lz++) {
                int wz = chunkZ * 16 + lz;
                if (wz < 0) {
                    continue;
                }
                int floor = wz / spacing;
                int z = wz - floor * spacing;
                if (z >= size) {
                    continue;
                }
                fillColumn(data, lx, lz, x, z, plan(floor));
            }
        }
    }

    /**
     * 構造の列を書く。Y: baseY=基礎床 / baseY+1=床面 / baseY+2〜+4=空間（壁は固体）/ baseY+5=天井。
     * x, z はフロア内ローカル座標（0〜size-1）。
     */
    private void fillColumn(ChunkData data, int lx, int lz, int x, int z, FloorPlan plan) {
        int floorY = baseY + 1;
        int topAir = baseY + 4;
        int ceilY = baseY + 5;

        data.setBlock(lx, baseY, lz, Material.STONE_BRICKS);
        data.setBlock(lx, ceilY, lz, Material.STONE_BRICKS);

        Material floorMat = null;   // 床面の素材（null=壁の柱）
        boolean wall = false;       // 床面〜空間を壁で埋める

        boolean outer = x == 0 || z == 0 || x == size - 1 || z == size - 1;
        int rx = x % cell;
        int rz = z % cell;
        boolean xLine = rx == 0;
        boolean zLine = rz == 0;

        if (outer || (xLine && zLine)) {
            wall = true;
        } else if (!xLine && !zLine) {
            int gx = x / cell;
            int gz = z / cell;
            floorMat = plan.getRoomFloor(gx, gz);
            // 部屋四隅の天井にグロウストーン
            if ((rx == 1 || rx == inner) && (rz == 1 || rz == inner)) {
                data.setBlock(lx, ceilY, lz, Material.GLOWSTONE);
            }
        } else if (xLine) {
            // 左右の部屋を隔てる壁。通路(3ブロック)があれば床を張って空ける
            int gx = x / cell;
            int gz = z / cell;
            MazeGenerator m = plan.getMaze();
            boolean door = gx > 0 && gx < grid && !m.hasWallBetween(gx, gz, gx - 1, gz)
                    && rz >= inner / 2 && rz <= inner / 2 + 2;
            if (door) {
                floorMat = Material.STONE_BRICKS;
            } else {
                wall = true;
            }
        } else {
            int gx = x / cell;
            int gz = z / cell;
            MazeGenerator m = plan.getMaze();
            boolean door = gz > 0 && gz < grid && !m.hasWallBetween(gx, gz, gx, gz - 1)
                    && rx >= inner / 2 && rx <= inner / 2 + 2;
            if (door) {
                floorMat = Material.STONE_BRICKS;
            } else {
                wall = true;
            }
        }

        if (wall) {
            for (int y = floorY; y <= topAir; y++) {
                data.setBlock(lx, y, lz, Material.STONE_BRICKS);
            }
        } else {
            data.setBlock(lx, floorY, lz, floorMat);
        }
    }
}
