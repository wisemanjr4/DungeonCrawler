package com.dungeoncrawler.npc;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 鍛冶屋GUI（仕様8.4）。
 * 装備を強化（覚醒）/ 素材を換金 / 素材情報。
 */
public class BlacksmithGui {

    public static final String TITLE = "§d§l鍛冶屋";

    private final DepthCrawlerPlugin plugin;

    public BlacksmithGui(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);
        gui.setItem(11, button(Material.ANVIL, "§6装備を強化",
                "手持ち武器を覚醒（素材1個+200G消費）", "→ /weaponly awaken"));
        gui.setItem(13, button(Material.GOLD_INGOT, "§e素材を換金",
                "インベントリ内の全素材アイテムをゴールド化", ""));
        gui.setItem(15, button(Material.BOOK, "§7素材情報",
                "ダンジョンで入手した素材の説明", ""));
        player.openInventory(gui);
    }

    /**
     * クリック処理。返り値: 処理したか
     */
    public boolean handleClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) {
            return false;
        }
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        switch (name) {
            case "装備を強化" -> {
                player.closeInventory();
                if (plugin.getServer().getPluginManager().getPlugin("Weaponly") != null) {
                    player.performCommand("weaponly awaken");
                } else {
                    player.sendMessage("§cWeaponly プラグインが導入されていません。");
                }
                return true;
            }
            case "素材を換金" -> {
                player.closeInventory();
                double total = convertMaterials(player);
                player.sendMessage(total > 0
                        ? ChatColor.GOLD + "素材を " + (int) total + "G に換金しました。"
                        : ChatColor.GRAY + "換金できる素材がありません。");
                return true;
            }
            case "素材情報" -> {
                player.sendMessage("§7=== 素材情報 ===");
                player.sendMessage("§e素材: §7ダンジョン内のMOBや宝箱から入手。");
                player.sendMessage("§e用途: §7鍛冶屋で換金、またはWeaponlyの覚醒素材として使用。");
                player.sendMessage("§e注意: §7死亡時は未保険の素材はロストします。");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * インベントリ内の素材アイテムを換金。返り値: 換金額
     */
    private double convertMaterials(Player player) {
        double total = 0;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) {
                continue;
            }
            if (plugin.getItemRegistry().isMaterial(item)) {
                double value = plugin.getItemRegistry().getValue(item);
                total += value * item.getAmount();
                player.getInventory().setItem(i, null);
            }
        }
        if (total > 0) {
            plugin.getPlayerDataManager().get(player.getUniqueId()).addBalance(total);
        }
        return total;
    }

    private ItemStack button(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        for (String line : loreLines) {
            if (line.contains("\n")) {
                for (String part : line.split("\n")) {
                    lore.add(part);
                }
            } else if (!line.isEmpty()) {
                lore.add(line);
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
