package com.dungeoncrawler.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MazeGenerator {

    private final int grid;
    private final boolean[][] visited;
    private final boolean[][] hWalls; // horizontal walls between rows (grid-1) x grid
    private final boolean[][] vWalls; // vertical walls between cols grid x (grid-1)

    public MazeGenerator(int grid) {
        this.grid = grid;
        this.visited = new boolean[grid][grid];
        this.hWalls = new boolean[grid - 1][grid];
        this.vWalls = new boolean[grid][grid - 1];
        for (int i = 0; i < grid - 1; i++) {
            for (int j = 0; j < grid; j++) {
                hWalls[i][j] = true;
            }
        }
        for (int i = 0; i < grid; i++) {
            for (int j = 0; j < grid - 1; j++) {
                vWalls[i][j] = true;
            }
        }
    }

    /**
     * DFSで迷路を生成。全部屋の接続を保証する。
     */
    public void generate(int startX, int startZ) {
        dfs(startX, startZ);
    }

    private void dfs(int x, int z) {
        visited[x][z] = true;
        List<int[]> dirs = new ArrayList<>();
        dirs.add(new int[]{1, 0});
        dirs.add(new int[]{-1, 0});
        dirs.add(new int[]{0, 1});
        dirs.add(new int[]{0, -1});
        Collections.shuffle(dirs);

        for (int[] d : dirs) {
            int nx = x + d[0];
            int nz = z + d[1];
            if (nx < 0 || nx >= grid || nz < 0 || nz >= grid || visited[nx][nz]) {
                continue;
            }
            carve(x, z, nx, nz);
            dfs(nx, nz);
        }
    }

    private void carve(int x1, int z1, int x2, int z2) {
        if (x1 != x2) {
            int row = Math.min(x1, x2);
            hWalls[row][z1] = false;
        } else {
            int col = Math.min(z1, z2);
            vWalls[x1][col] = false;
        }
    }

    /**
     * 指定セルが接続先を持つかどうか（マージ処理用）
     */
    public boolean hasWallBetween(int x1, int z1, int x2, int z2) {
        if (x1 != x2) {
            int row = Math.min(x1, x2);
            return hWalls[row][z1];
        } else {
            int col = Math.min(z1, z2);
            return vWalls[x1][col];
        }
    }

    public boolean isHorizWall(int row, int col) {
        return hWalls[row][col];
    }

    public boolean isVertWall(int row, int col) {
        return vWalls[row][col];
    }

    public int getGrid() {
        return grid;
    }

    public List<int[]> connections(int x, int z) {
        List<int[]> result = new ArrayList<>();
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            int nx = x + d[0];
            int nz = z + d[1];
            if (nx < 0 || nx >= grid || nz < 0 || nz >= grid) {
                continue;
            }
            if (!hasWallBetween(x, z, nx, nz)) {
                result.add(new int[]{nx, nz});
            }
        }
        return result;
    }
}
