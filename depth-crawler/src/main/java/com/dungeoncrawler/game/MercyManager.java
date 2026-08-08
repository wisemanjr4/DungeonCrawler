package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MercyManager {

    private final DepthCrawlerPlugin plugin;

    public MercyManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 情けを乞う。装備全没収 → 仮初装備一式 + 食料支給（1回/ラン/人）
     */
    public boolean grantMercy(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage("§cダンジョン内でのみ利用できます。");
            return false;
        }
        if (session.isMerciesUsed(player.getUniqueId())) {
            player.sendMessage("§cこのランではすでに情けを利用しました。");
            return false;
        }
        session.setMerciesUsed(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
        player.getInventory().addItem(new ItemStack(Material.APPLE, 8));
        player.sendMessage("§a情けを受けました。仮初装備一式と食料を支給します。");
        return true;
    }
}
