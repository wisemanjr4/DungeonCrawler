package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpgradeManager {

    public static final String UPGRADE_GUI_TITLE = "§5§lアップグレード選択";

    private final DepthCrawlerPlugin plugin;
    private final Map<UUID, Long> guiOpenCooldown = new java.util.concurrent.ConcurrentHashMap<>();

    public UpgradeManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 休息フロアの石ボタンで開くアップグレードGUI。3択。
     */
    public void openUpgradeGui(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null || !session.isRestFloor()) {
            player.sendMessage("§c休息フロアでのみ利用できます。");
            return;
        }

        List<UpgradeType> available = new ArrayList<>();
        for (UpgradeType type : UpgradeType.values()) {
            if (session.getUpgradeLevel(type) < type.getMaxLevel()) {
                available.add(type);
            }
        }
        if (available.isEmpty()) {
            player.sendMessage("§c取得可能なアップグレードがありません。");
            return;
        }

        Collections.shuffle(available);
        List<UpgradeType> choices = available.subList(0, Math.min(3, available.size()));

        Inventory gui = Bukkit.createInventory(null, 27, UPGRADE_GUI_TITLE);
        int slot = 11;
        for (UpgradeType choice : choices) {
            ItemStack item = new ItemStack(choice.isRare() ? Material.GOLD_INGOT : Material.IRON_INGOT);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + choice.getName());
            int level = session.getUpgradeLevel(choice) + 1;
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + choice.getDescription());
            lore.add(ChatColor.AQUA + "Lv" + level + " / 最大Lv" + choice.getMaxLevel());
            if (choice.isRare()) {
                lore.add(ChatColor.RED + "★レア");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot, item);
            slot += 3;
        }
        player.openInventory(gui);
    }

    /**
     * GUIクリック処理。
     */
    public boolean handleClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) {
            return false;
        }
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        for (UpgradeType type : UpgradeType.values()) {
            if (type.getName().equals(name)) {
                DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
                if (session != null) {
                    session.addUpgrade(type);
                    player.sendMessage("§aアップグレード取得: §6" + type.getName());
                    player.closeInventory();
                }
                return true;
            }
        }
        return false;
    }
}
