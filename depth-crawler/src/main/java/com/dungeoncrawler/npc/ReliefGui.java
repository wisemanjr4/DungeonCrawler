package com.dungeoncrawler.npc;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.game.InsuranceManager;
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
 * 救助班GUI。情け / 保険(1個) / 全装備一括保険 / 保険回収。
 */
public class ReliefGui {

    public static final String TITLE = "§b§l救助班";

    private final DepthCrawlerPlugin plugin;

    public ReliefGui(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);
        gui.setItem(10, button(Material.IRON_HELMET, "§b情けを乞う",
                "装備全没収 → 仮初装備一式 + 食料支給", "1回/ラン"));
        gui.setItem(12, button(Material.GOLD_INGOT, "§6保険を掛ける(1個)",
                "手持ちアイテムに保険 → 業者選択 → 契約", ""));
        gui.setItem(14, button(Material.DIAMOND_CHESTPLATE, "§6全装備一括保険",
                "全武器/防具を検出 → 業者選択 → 一括契約", ""));
        gui.setItem(16, button(Material.CHEST, "§a保険を回収",
                "返還待ちの保険アイテムを回収", "/dungeon claim"));
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
            case "情けを乞う" -> {
                plugin.getMercyManager().grantMercy(player);
                return true;
            }
            case "保険を掛ける(1個)" -> {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    player.sendMessage("§c手持ちアイテムが必要です。");
                    return true;
                }
                openProviderSelect(player, false);
                return true;
            }
            case "全装備一括保険" -> {
                openProviderSelect(player, true);
                return true;
            }
            case "保険を回収" -> {
                player.closeInventory();
                var claimable = plugin.getInsuranceManager().getClaimable(player);
                if (claimable.isEmpty()) {
                    player.sendMessage("§c回収できる保険アイテムがありません。");
                    return true;
                }
                int claimed = 0;
                for (var entry : claimable) {
                    if (plugin.getInsuranceManager().claim(player, entry)) {
                        claimed++;
                    }
                }
                player.sendMessage("§a" + claimed + "件の保険アイテムを回収しました。");
                return true;
            }
            default -> {
                return handleProviderClick(player, name, clicked);
            }
        }
    }

    public static final String PROVIDER_TITLE = "§5§l保険業者選択";

    private void openProviderSelect(Player player, boolean all) {
        Inventory gui = Bukkit.createInventory(null, 27, PROVIDER_TITLE);
        InsuranceManager.Provider[] providers = InsuranceManager.Provider.values();
        for (int i = 0; i < providers.length && i < 5; i++) {
            InsuranceManager.Provider p = providers[i];
            Material mat = Material.matchMaterial(p.getIcon());
            if (mat == null) {
                mat = Material.GOLD_BLOCK;
            }
            List<String> lore = new ArrayList<>();
            lore.add("§7料金率: " + (int) (p.getFeeRate() * 100) + "%");
            lore.add("§7返還率: " + (int) (p.getReturnRate() * 100) + "%");
            lore.add("§7返還時間: " + formatTime(p.getReturnTime()));
            ItemStack item = button(mat, p.getDisplay(), String.join("\n", lore), "");
            gui.setItem(10 + i * 2, item);
        }
        player.openInventory(gui);
        // 一括か個別かはコマンド引数で区別できないため、クリック後に再度判定する
        player.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "relief_all"),
                org.bukkit.persistence.PersistentDataType.INTEGER, all ? 1 : 0);
    }

    private boolean handleProviderClick(Player player, String name, ItemStack clicked) {
        InsuranceManager.Provider provider = null;
        for (InsuranceManager.Provider p : InsuranceManager.Provider.values()) {
            if (ChatColor.stripColor(p.getDisplay()).equals(name)) {
                provider = p;
                break;
            }
        }
        if (provider == null) {
            return false;
        }
        player.closeInventory();
        var key = new org.bukkit.NamespacedKey(plugin, "relief_all");
        Integer all = player.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.INTEGER);
        player.getPersistentDataContainer().remove(key);
        if (all != null && all == 1) {
            plugin.getInsuranceManager().insureAllEquipment(player, provider);
        } else {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir()) {
                player.sendMessage("§c手持ちアイテムが必要です。");
                return true;
            }
            plugin.getInsuranceManager().insure(player, hand, provider);
        }
        return true;
    }

    private String formatTime(long millis) {
        long hours = millis / (60 * 60 * 1000L);
        if (hours < 24) {
            return hours + "時間";
        }
        return (hours / 24) + "日";
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
