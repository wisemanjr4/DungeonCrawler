package com.dungeoncrawler.command;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetupCommand {

    public static boolean handle(Player player, String[] args, DepthCrawlerPlugin plugin) {
        if (!player.hasPermission("depthcrawler.admin")) {
            player.sendMessage(ChatColor.RED + "権限がありません。");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§c/dungeon setup <spawn|dungeon|shop|arena|wall>");
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "spawn" -> {
                plugin.getDungeonBuilder().buildSpawn(player.getWorld(), player.getLocation());
                player.sendMessage("§a拠点エリアを生成しました（25×25）。");
            }
            case "dungeon" -> {
                plugin.getDungeonBuilder().buildDungeonEntrance(player.getWorld(), player.getLocation());
                player.sendMessage("§aダンジョン入口エリアを生成しました。");
            }
            case "shop" -> {
                plugin.getDungeonBuilder().buildShop(player.getWorld(), player.getLocation());
                player.sendMessage("§aショップエリアを生成しました。");
            }
            case "arena" -> {
                int w = 20, d = 20;
                if (args.length >= 3) {
                    try {
                        w = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (args.length >= 4) {
                    try {
                        d = Integer.parseInt(args[3]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                plugin.getDungeonBuilder().buildArena(player.getWorld(), player.getLocation(), w, d);
                player.sendMessage("§a闘技場を生成しました（" + w + "x" + d + "）。");
            }
            case "wall" -> {
                var selection = plugin.getDungeonBuilder().getSelection(player.getUniqueId());
                if (selection[0] == null || selection[1] == null) {
                    player.sendMessage("§c/db pos1, pos2 で範囲を選択してください。");
                    return true;
                }
                plugin.getDungeonBuilder().buildWallsAround(player.getWorld(), selection[0], selection[1]);
                player.sendMessage("§a周囲を壁で囲みました。");
            }
            default -> player.sendMessage("§c不明なサブコマンドです。");
        }
        return true;
    }
}
