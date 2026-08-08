package com.dungeoncrawler.listener;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.npc.NpcType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NpcListener implements Listener {

    private final DepthCrawlerPlugin plugin;

    public NpcListener(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNpcInteract(PlayerInteractEntityEvent event) {
        NpcType type = plugin.getNpcManager().getType(event.getRightClicked());
        if (type == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();

        switch (type) {
            case SHOP -> plugin.getShopManager().openMenu(player);
            case SAFEBOX -> plugin.getSafeBoxManager().open(player, 1);
            case DUNGEON_GUIDE -> openDungeonMenu(player);
            case BLACKSMITH -> plugin.getBlacksmithGui().open(player);
            case RELIEF -> plugin.getReliefGui().open(player);
            case BLACKMARKET -> plugin.getBlackMarketManager().openMenu(player);
        }
    }

    private void openDungeonMenu(Player player) {
        player.sendMessage("§6§l=== ダンジョン案内人 ===");
        player.sendMessage("§e[1] 潜る §7→ /dungeon enter");
        player.sendMessage("§e[2] パーティ §7→ /dungeon party invite <player>");
        player.sendMessage("§e[3] やめる §7→ 会話終了");
    }
}
