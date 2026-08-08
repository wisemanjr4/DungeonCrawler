package com.dungeoncrawler.command;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.item.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShowItemCommand implements CommandExecutor {

    private final DepthCrawlerPlugin plugin;

    public ShowItemCommand(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です。");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§c手持ちアイテムが必要です。");
            return true;
        }
        if (!plugin.getItemRegistry().isDcItem(item)) {
            player.sendMessage("§7このアイテムはDC装備ではありません。");
            return true;
        }
        String display = item.getItemMeta().getDisplayName();
        String rarity = plugin.getItemRegistry().getRarity(item);
        double value = plugin.getItemRegistry().getValue(item);

        String message = "§8[§6SHOW§8] §7" + player.getName() + ": " + display
                + " §7[" + rarity + "] §7価値 " + (int) value + "G";
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(message);
        }
        return true;
    }
}
