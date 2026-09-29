package com.dungeoncrawler;

import com.dungeoncrawler.maze.MazeGenerator;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MazeGeneratorTest {

    @RepeatedTest(50)
    void allRoomsReachableFromStartAndTreeShaped() {
        int grid = 10;
        MazeGenerator maze = new MazeGenerator(grid);
        maze.generate(0, 0);

        boolean[][] seen = new boolean[grid][grid];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{0, 0});
        seen[0][0] = true;
        int reached = 0;
        int edges = 0;
        while (!queue.isEmpty()) {
            int[] c = queue.poll();
            reached++;
            for (int[] n : maze.connections(c[0], c[1])) {
                edges++;
                if (!seen[n[0]][n[1]]) {
                    seen[n[0]][n[1]] = true;
                    queue.add(n);
                }
            }
        }
        assertEquals(grid * grid, reached, "全部屋が接続されている");
        assertEquals(grid * grid - 1, edges / 2, "DFS迷路は全域木（辺数=部屋数-1）");
    }
}
