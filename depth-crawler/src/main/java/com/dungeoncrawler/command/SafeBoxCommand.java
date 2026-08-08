package com.dungeoncrawler.command;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SafeBoxCommand implements CommandExecutor {

    private final DepthCrawlerPlugin plugin;

    public SafeBoxCommand(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です。");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("deposit")) {
            boolean ok = plugin.getSafeBoxManager().deposit(player);
            player.sendMessage(ok ? "§a手持ちアイテムを預入しました。" : "§c預入できませんでした（容量超過など）。");
            return true;
        }
        plugin.getSafeBoxManager().open(player, 1);
        return true;
    }
}
