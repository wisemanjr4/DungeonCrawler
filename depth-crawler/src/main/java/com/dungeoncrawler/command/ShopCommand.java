package com.dungeoncrawler.command;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.player.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final DepthCrawlerPlugin plugin;

    public ShopCommand(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です。");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("balance")) {
            PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
            player.sendMessage("§6所持金: §e" + (int) data.getBalance() + "G");
            return true;
        }
        plugin.getShopManager().openMenu(player);
        return true;
    }
}
