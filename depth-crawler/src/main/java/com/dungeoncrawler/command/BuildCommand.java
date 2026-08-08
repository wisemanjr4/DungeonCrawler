package com.dungeoncrawler.command;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 建築ツール（/db）。管理者向け。すべての変更をundo履歴に記録する。
 */
public class BuildCommand implements CommandExecutor {

    private final DepthCrawlerPlugin plugin;
    private final Map<UUID, Material> defaultMaterial = new HashMap<>();
    private final Map<UUID, Deque<List<BlockState>>> undoHistory = new HashMap<>();

    public BuildCommand(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です。");
            return true;
        }
        if (!player.hasPermission("depthcrawler.admin")) {
            player.sendMessage(ChatColor.RED + "権限がありません。");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§6§l=== ビルドコマンド ===");
            player.sendMessage("§e/db guide §7- 建築ガイド");
            player.sendMessage("§e/db pos1, pos2 §7- 範囲選択");
            player.sendMessage("§e/db material <素材> §7- 素材変更");
            player.sendMessage("§e/db room <w> <d> §7- 部屋生成");
            player.sendMessage("§e/db corridor <x> <z> §7- 通路");
            player.sendMessage("§e/db doorway §7- 2x3通路");
            player.sendMessage("§e/db platform <w> <d> §7- 足場");
            player.sendMessage("§e/db wall §7- 壁");
            player.sendMessage("§e/db torch <間隔> §7- 松明");
            player.sendMessage("§e/db stairs <方角> <段数> §7- 階段");
            player.sendMessage("§e/db fountain §7- 噴水");
            player.sendMessage("§e/db chest §7- チェスト");
            player.sendMessage("§e/db pillar <高さ> §7- 柱");
            player.sendMessage("§e/db maze <w> <h> §7- 迷路");
            player.sendMessage("§e/db undo §7- 1手戻す");
            player.sendMessage("§e/db reset §7- リセット");
            return true;
        }

        DungeonBuilder builder = plugin.getDungeonBuilder();
        switch (args[0].toLowerCase()) {
            case "guide" -> player.sendMessage("§7ガイド: pos1→pos2で範囲選択。room/wall等で建築。undoで1手戻す。");
            case "pos1" -> {
                builder.setSelection(player.getUniqueId(), 0, player.getLocation());
                player.sendMessage("§aPos1 を設定しました。");
            }
            case "pos2" -> {
                builder.setSelection(player.getUniqueId(), 1, player.getLocation());
                player.sendMessage("§aPos2 を設定しました。");
            }
            case "material" -> {
                if (args.length < 2) {
                    player.sendMessage("§c/db material <素材>");
                    return true;
                }
                Material mat = Material.matchMaterial(args[1].toUpperCase());
                if (mat == null) {
                    player.sendMessage("§c不明な素材です。");
                    return true;
                }
                defaultMaterial.put(player.getUniqueId(), mat);
                player.sendMessage("§aデフォルト素材: " + mat.name());
            }
            case "room" -> {
                int w = 10, d = 10;
                if (args.length >= 2) w = parseInt(args[1], w);
                if (args.length >= 3) d = parseInt(args[2], d);
                buildRoom(player, w, d);
            }
            case "corridor" -> {
                int x = 5, z = 3;
                if (args.length >= 2) x = parseInt(args[1], x);
                if (args.length >= 3) z = parseInt(args[2], z);
                buildCorridor(player, x, z);
            }
            case "doorway" -> buildDoorway(player);
            case "platform" -> {
                int w = 5, d = 5;
                if (args.length >= 2) w = parseInt(args[1], w);
                if (args.length >= 3) d = parseInt(args[2], d);
                buildPlatform(player, w, d);
            }
            case "wall" -> buildWall(player);
            case "torch" -> {
                int interval = 5;
                if (args.length >= 2) interval = parseInt(args[1], interval);
                buildTorchLine(player, interval);
            }
            case "stairs" -> {
                String dir = args.length >= 2 ? args[1] : "north";
                int steps = args.length >= 3 ? parseInt(args[2], 5) : 5;
                buildStairs(player, dir, steps);
            }
            case "fountain" -> buildFountain(player);
            case "chest" -> {
                Block block = player.getTargetBlockExact(5);
                if (block != null) {
                    snapshot(player);
                    block.setType(Material.CHEST);
                    commit(player);
                    player.sendMessage("§aチェストを設置しました。");
                } else {
                    player.sendMessage("§cブロックが見えていません。");
                }
            }
            case "pillar" -> {
                int height = args.length >= 2 ? parseInt(args[1], 5) : 5;
                buildPillar(player, height);
            }
            case "maze" -> {
                int w = 10, h = 10;
                if (args.length >= 2) w = parseInt(args[1], w);
                if (args.length >= 3) h = parseInt(args[2], h);
                buildMaze(player, w, h);
            }
            case "undo" -> undo(player);
            case "reset" -> {
                builder.clearSelection(player.getUniqueId());
                defaultMaterial.remove(player.getUniqueId());
                undoHistory.remove(player.getUniqueId());
                player.sendMessage("§aビルド状態をリセットしました。");
            }
            default -> player.sendMessage("§c不明なサブコマンドです。§e/db で一覧。");
        }
        return true;
    }

    private Material mat(Player player) {
        return defaultMaterial.getOrDefault(player.getUniqueId(), Material.STONE_BRICKS);
    }

    /**
     * undo履歴の開始。この後 setBlock した変更が記録される。
     */
    private void snapshot(Player player) {
        undoHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>()).push(new ArrayList<>());
    }

    /**
     * 現在のスナップショットにブロック変更を記録。
     */
    private void record(Player player, Block block) {
        Deque<List<BlockState>> history = undoHistory.get(player.getUniqueId());
        if (history != null && !history.isEmpty()) {
            history.peek().add(block.getState());
        }
    }

    /**
     * undoスナップショットを確定。
     */
    private void commit(Player player) {
        // スタックが増えすぎないよう上限を保つ
        Deque<List<BlockState>> history = undoHistory.get(player.getUniqueId());
        if (history != null && history.size() > 100) {
            history.removeLast();
        }
    }

    /**
     * 1手戻す。
     */
    private void undo(Player player) {
        Deque<List<BlockState>> history = undoHistory.get(player.getUniqueId());
        if (history == null || history.isEmpty()) {
            player.sendMessage("§c元に戻す履歴がありません。");
            return;
        }
        List<BlockState> changes = history.pop();
        int restored = 0;
        for (BlockState state : changes) {
            state.update(true, false);
            restored++;
        }
        player.sendMessage("§a" + restored + " ブロックを元に戻しました。");
    }

    /**
     * 変更対象ブロックを記録しながら setType。
     */
    private void setType(Player player, Block block, Material material) {
        record(player, block);
        block.setType(material);
    }

    private void buildRoom(Player player, int w, int d) {
        snapshot(player);
        int ox = player.getLocation().getBlockX();
        int oy = player.getLocation().getBlockY();
        int oz = player.getLocation().getBlockZ();
        Material m = mat(player);
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                setType(player, player.getWorld().getBlockAt(ox + x, oy - 1, oz + z), m);
                if (x == 0 || x == w - 1 || z == 0 || z == d - 1) {
                    for (int y = 0; y < 4; y++) {
                        setType(player, player.getWorld().getBlockAt(ox + x, oy + y, oz + z), m);
                    }
                }
            }
        }
        commit(player);
        player.sendMessage("§a部屋を生成しました（" + w + "x" + d + "）。");
    }

    private void buildCorridor(Player player, int x, int z) {
        snapshot(player);
        int oy = player.getLocation().getBlockY();
        int ox = player.getLocation().getBlockX();
        int oz = player.getLocation().getBlockZ();
        Material m = mat(player);
        for (int dx = 0; dx < x; dx++) {
            for (int dz = 0; dz < z; dz++) {
                setType(player, player.getWorld().getBlockAt(ox + dx, oy - 1, oz + dz), m);
            }
        }
        commit(player);
        player.sendMessage("§a通路を生成しました。");
    }

    private void buildDoorway(Player player) {
        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            player.sendMessage("§c壁が見えていません。");
            return;
        }
        snapshot(player);
        int x = target.getX();
        int y = target.getY();
        int z = target.getZ();
        // 向いてる方角に2×3の穴
        org.bukkit.block.BlockFace facing = player.getFacing();
        int dx = facing.getModX();
        int dz = facing.getModZ();
        for (int h = 0; h < 3; h++) {
            for (int w = -1; w <= 1; w++) {
                int wx = (dx == 0) ? w : dx;
                int wz = (dz == 0) ? w : dz;
                setType(player, player.getWorld().getBlockAt(x + wx, y + h, z + wz), Material.AIR);
            }
        }
        commit(player);
        player.sendMessage("§a2x3の通路を開けました。");
    }

    private void buildPlatform(Player player, int w, int d) {
        snapshot(player);
        int oy = player.getLocation().getBlockY();
        int ox = player.getLocation().getBlockX();
        int oz = player.getLocation().getBlockZ();
        Material m = mat(player);
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                setType(player, player.getWorld().getBlockAt(ox + x, oy - 1, oz + z), m);
            }
        }
        commit(player);
        player.sendMessage("§a足場を生成しました。");
    }

    private void buildWall(Player player) {
        var selection = plugin.getDungeonBuilder().getSelection(player.getUniqueId());
        if (selection[0] == null || selection[1] == null) {
            player.sendMessage("§c/db pos1, pos2 で範囲を選択してください。");
            return;
        }
        snapshot(player);
        Material m = mat(player);
        int minX = Math.min(selection[0].getBlockX(), selection[1].getBlockX());
        int maxX = Math.max(selection[0].getBlockX(), selection[1].getBlockX());
        int minY = Math.min(selection[0].getBlockY(), selection[1].getBlockY());
        int maxY = Math.max(selection[0].getBlockY(), selection[1].getBlockY());
        int minZ = Math.min(selection[0].getBlockZ(), selection[1].getBlockZ());
        int maxZ = Math.max(selection[0].getBlockZ(), selection[1].getBlockZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    setType(player, player.getWorld().getBlockAt(x, y, z), m);
                }
            }
        }
        commit(player);
        player.sendMessage("§a壁を生成しました。");
    }

    private void buildTorchLine(Player player, int interval) {
        var selection = plugin.getDungeonBuilder().getSelection(player.getUniqueId());
        if (selection[0] == null || selection[1] == null) {
            player.sendMessage("§c/db pos1, pos2 で範囲を選択してください。");
            return;
        }
        snapshot(player);
        int minX = Math.min(selection[0].getBlockX(), selection[1].getBlockX());
        int maxX = Math.max(selection[0].getBlockX(), selection[1].getBlockX());
        int minY = Math.min(selection[0].getBlockY(), selection[1].getBlockY());
        int maxY = Math.max(selection[0].getBlockY(), selection[1].getBlockY());
        int minZ = Math.min(selection[0].getBlockZ(), selection[1].getBlockZ());
        int maxZ = Math.max(selection[0].getBlockZ(), selection[1].getBlockZ());
        int count = 0;
        for (int x = minX; x <= maxX; x += interval) {
            for (int z = minZ; z <= maxZ; z += interval) {
                setType(player, player.getWorld().getBlockAt(x, maxY + 1, z), Material.TORCH);
                count++;
            }
        }
        commit(player);
        player.sendMessage("§a松明を" + count + "本設置しました。");
    }

    private void buildStairs(Player player, String direction, int steps) {
        snapshot(player);
        int ox = player.getLocation().getBlockX();
        int oy = player.getLocation().getBlockY();
        int oz = player.getLocation().getBlockZ();
        Material m = mat(player);
        for (int i = 0; i < steps; i++) {
            int x = ox, z = oz;
            switch (direction.toLowerCase()) {
                case "north" -> z -= i;
                case "south" -> z += i;
                case "east" -> x += i;
                case "west" -> x -= i;
                default -> {
                    undo(player);
                    player.sendMessage("§c方角は north/south/east/west。");
                    return;
                }
            }
            setType(player, player.getWorld().getBlockAt(x, oy + i, z), m);
        }
        commit(player);
        player.sendMessage("§a階段を生成しました。");
    }

    private void buildFountain(Player player) {
        snapshot(player);
        int ox = player.getLocation().getBlockX();
        int oy = player.getLocation().getBlockY();
        int oz = player.getLocation().getBlockZ();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                setType(player, player.getWorld().getBlockAt(ox + x, oy - 1, oz + z), Material.STONE_BRICKS);
            }
        }
        setType(player, player.getWorld().getBlockAt(ox, oy - 1, oz), Material.WATER);
        commit(player);
        player.sendMessage("§a噴水を生成しました。");
    }

    private void buildPillar(Player player, int height) {
        snapshot(player);
        int ox = player.getLocation().getBlockX();
        int oy = player.getLocation().getBlockY();
        int oz = player.getLocation().getBlockZ();
        Material m = mat(player);
        for (int y = 0; y < height; y++) {
            setType(player, player.getWorld().getBlockAt(ox, oy + y, oz), m);
        }
        commit(player);
        player.sendMessage("§a柱を生成しました。");
    }

    private void buildMaze(Player player, int w, int h) {
        com.dungeoncrawler.maze.MazeGenerator maze = new com.dungeoncrawler.maze.MazeGenerator(w);
        maze.generate(0, 0);
        snapshot(player);
        Material m = mat(player);
        int ox = player.getLocation().getBlockX();
        int oz = player.getLocation().getBlockZ();
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < h; z++) {
                setType(player, player.getWorld().getBlockAt(ox + x, player.getLocation().getBlockY() - 1, oz + z), m);
            }
        }
        commit(player);
        player.sendMessage("§a迷路の骨組みを生成しました。");
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
