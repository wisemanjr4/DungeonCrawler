package com.dungeoncrawler.listener;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.game.LootManager;
import com.dungeoncrawler.game.FloorModifier;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ChestListener implements Listener {

    private final DepthCrawlerPlugin plugin;

    public ChestListener(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChestInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!(event.getClickedBlock().getState() instanceof Chest chest)) {
            return;
        }
        Player player = event.getPlayer();
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }

        // 一度ルートを生成したチェストは再ロールしない（カスタム名で判定）
        if (chest.getCustomName() != null) {
            return;
        }

        // 宝箱ストリーク更新
        plugin.getChestStreakManager().onChestOpened(player);

        // ルート生成
        FloorModifier modifier = session.getModifier();
        double dropMult = modifier.getDropMult();
        if (modifier == FloorModifier.TREASURE) {
            dropMult *= 2.0;
        }
        double qualityBonus = plugin.getChestStreakManager().getQualityBonus(player);

        Inventory inv = chest.getInventory();
        inv.clear();
        chest.setCustomName("§6§l宝箱");
        chest.update();
        List<ItemStack> loot = plugin.getLootManager().rollLoot(session.getFloor(), dropMult, qualityBonus);
        for (ItemStack item : loot) {
            if (item != null) {
                inv.addItem(item);
            }
        }
        player.sendMessage("§a宝箱を開けました。中身を取り出してください。");
    }
}
