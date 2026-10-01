package com.dungeoncrawler.dungeon;

import com.dungeoncrawler.maze.MazeGenerator;
import org.bukkit.Material;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 1フロア分の構造データ（迷路 + 部屋ごとの床材）。生成後は不変。
 * チャンク生成スレッドから読まれるため、生成時に確定させてから公開する。
 */
public final class FloorPlan {

    private final MazeGenerator maze;
    private final Material[][] roomFloor;

    public FloorPlan(int grid) {
        this.maze = new MazeGenerator(grid);
        this.maze.generate(0, 0);
        this.roomFloor = new Material[grid][grid];
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                double r = rnd.nextDouble();
                roomFloor[gx][gz] = r < 0.4 ? Material.POLISHED_ANDESITE
                        : (r < 0.75 ? Material.STONE_BRICKS : Material.TERRACOTTA);
            }
        }
    }

    public MazeGenerator getMaze() {
        return maze;
    }

    public Material getRoomFloor(int gx, int gz) {
        return roomFloor[gx][gz];
    }
}
