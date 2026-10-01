package com.dungeoncrawler.dungeon;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;


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
     * 構造（床・壁・天井）は {@link VoidChunkGenerator} がチャンク生成時に書くため、
     * ここでは装飾（宝箱・柱・出口・ボタン）だけを置く。
     */
    public void generate(World world, int floor, boolean restFloor, boolean bossFloor, boolean treasure) {
        VoidChunkGenerator generator = plugin.getDungeonManager().getGenerator(world);
        if (generator == null) {
            throw new IllegalStateException("ダンジョン用ジェネレーターがないワールドです: " + world.getName());
        }
        generator.plan(floor); // 構造を確定（チャンク生成前に決めておく）
        decorate(world, floorStartZ(floor), restFloor, bossFloor, treasure);
    }

    /**
     * 非同期版: フロア全体のチャンクを先にバックグラウンドで生成・読み込みしてから、
     * メインスレッドで装飾を置き、onDone を呼ぶ。生成中もサーバーは止まらない。
     */
    public void generateAsync(World world, int floor, boolean restFloor, boolean bossFloor, boolean treasure,
                              Runnable onDone) {
        VoidChunkGenerator generator = plugin.getDungeonManager().getGenerator(world);
        if (generator == null) {
            throw new IllegalStateException("ダンジョン用ジェネレーターがないワールドです: " + world.getName());
        }
        generator.plan(floor);
        int originZ = floorStartZ(floor);
        int size = getFloorSize();
        java.util.List<java.util.concurrent.CompletableFuture<?>> futures = new java.util.ArrayList<>();
        for (int cx = 0; cx <= (size - 1) >> 4; cx++) {
            for (int cz = originZ >> 4; cz <= (originZ + size - 1) >> 4; cz++) {
                futures.add(world.getChunkAtAsync(cx, cz, true));
            }
        }
        java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0]))
                .whenComplete((ok, err) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (err != null) {
                        plugin.getLogger().warning("フロア生成に失敗: " + err);
                        return;
                    }
                    decorate(world, originZ, restFloor, bossFloor, treasure);
                    onDone.run();
                }));
    }

    private void decorate(World world, int originZ, boolean restFloor, boolean bossFloor, boolean treasure) {
        int floorY = baseY + 1;      // 床面
        int standY = baseY + 2;      // 床の上（宝箱・柱の最下段・ボタン）

        // 宝箱配置（TREASURE修飾子で2倍）。ランダムな部屋の中央の床の上に設置。
        double chestChance = treasure ? 0.30 : 0.15;
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                if (gx == grid - 1 && gz == grid - 1) {
                    continue; // 出口部屋には置かない
                }
                if (Math.random() < chestChance) {
                    int cx = gx * cell + inner / 2 + 1;
                    int cz = gz * cell + inner / 2 + 1;
                    world.getBlockAt(cx, standY, originZ + cz).setType(Material.CHEST, false);
                }
            }
        }

        // 30%の部屋の中央に柱 + 松明
        for (int gx = 0; gx < grid; gx++) {
            for (int gz = 0; gz < grid; gz++) {
                // 出口部屋と、スポーン地点のある開始部屋(0,0)には柱を置かない
                if (Math.random() < 0.30 && !(gx == grid - 1 && gz == grid - 1) && !(gx == 0 && gz == 0)) {
                    int cx = gx * cell + inner / 2 + 1;
                    int cz = gz * cell + inner / 2 + 1;
                    // 柱はチェストと重なる場合スキップ
                    if (world.getBlockAt(cx, standY, originZ + cz).getType() == Material.CHEST) {
                        continue;
                    }
                    for (int y = standY; y <= standY + 1; y++) {
                        world.getBlockAt(cx, y, originZ + cz).setType(Material.STONE_BRICKS, false);
                    }
                    world.getBlockAt(cx, standY + 2, originZ + cz).setType(Material.TORCH, false);
                }
            }
        }

        // 出口部屋（右下の部屋）: エメラルド=次フロア、ゴールド=帰還（全フロア共通、仕様3.5）
        int ox = (grid - 1) * cell + inner / 2 + 1;
        int oz = (grid - 1) * cell + inner / 2 + 1;
        world.getBlockAt(ox, floorY, originZ + oz).setType(Material.EMERALD_BLOCK, false);
        world.getBlockAt(ox + 2, floorY, originZ + oz).setType(Material.GOLD_BLOCK, false);

        if (bossFloor) {
            // BOSSフロアの目印: 出口部屋にレッドストーンブロック
            world.getBlockAt(ox - 4, floorY, originZ + oz).setType(Material.REDSTONE_BLOCK, false); // 休息と重なるF10でもビーコンと被らない位置
        }
        if (restFloor) {
            // 休息フロアの目印: ビーコン
            world.getBlockAt(ox - 2, floorY, originZ + oz).setType(Material.BEACON, false);
            // 石ボタン（東の外壁 x+1 に取り付け）→ アップグレードGUI
            int wallX = (grid - 1) * cell + inner + 1;
            org.bukkit.block.Block button = world.getBlockAt(wallX - 1, standY, originZ + oz);
            button.setType(Material.STONE_BUTTON, false);
            org.bukkit.block.data.type.Switch switchData = (org.bukkit.block.data.type.Switch) button.getBlockData();
            switchData.setFacing(org.bukkit.block.BlockFace.WEST); // 東壁に取り付く＝西を向く
            button.setBlockData(switchData, false);
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
