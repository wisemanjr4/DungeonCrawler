package com.dungeoncrawler.npc;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.player.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ブラックマーケット: 死亡時流出品を購入できる。
 * ダンジョン内で死亡した際のアイテムを記録し、ブラックマーケットNPCで購入する。
 */
public class BlackMarketManager {

    public static final String TITLE = "§5§lブラックマーケット";

    private final DepthCrawlerPlugin plugin;
    private final List<BlackMarketItem> stock = new ArrayList<>();

    public BlackMarketManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    public static class BlackMarketItem {
        private final String id;
        private final ItemStack item;
        private final double price;
        private final long addedAt;
        private final String ownerName;

        public BlackMarketItem(String id, ItemStack item, double price, long addedAt, String ownerName) {
            this.id = id;
            this.item = item;
            this.price = price;
            this.addedAt = addedAt;
            this.ownerName = ownerName;
        }

        public String getId() {
            return id;
        }

        public ItemStack getItem() {
            return item;
        }

        public double getPrice() {
            return price;
        }

        public long getAddedAt() {
            return addedAt;
        }

        public String getOwnerName() {
            return ownerName;
        }
    }

    /**
     * 死亡時にアイテムをブラックマーケットの在庫へ流す（50%の確率、価格は50%）。
     */
    public void addOnDeath(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        if (Math.random() < 0.5) {
            return; // 50%で流出しない
        }
        double value = plugin.getItemRegistry().getValue(item);
        double price = Math.max(5, value * 0.5);
        BlackMarketItem entry = new BlackMarketItem(
                UUID.randomUUID().toString(), item.clone(), price,
                System.currentTimeMillis(), player.getName());
        stock.add(entry);
        if (stock.size() > 200) {
            stock.remove(0);
        }
    }

    /**
     * GUIを開く。
     */
    public void openMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);
        int slot = 0;
        for (BlackMarketItem entry : stock) {
            if (slot >= 45) {
                break;
            }
            ItemStack display = entry.getItem().clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
            lore.add(ChatColor.GOLD + "価格: " + (int) entry.getPrice() + "G");
            lore.add(ChatColor.GRAY + "元所有者: " + entry.getOwnerName());
            meta.setLore(lore);
            display.setItemMeta(meta);
            gui.setItem(slot, display);
            slot++;
        }
        if (slot == 0) {
            ItemStack empty = ItemRegistryProxy.empty();
            gui.setItem(22, empty);
        }
        player.openInventory(gui);
    }

    /**
     * 購入処理。クリックしたスロット番号で在庫を特定する（表示用のコピーは値札ロアが付くため、
     * アイテムの見た目での照合は同名・メタ無しのアイテムで誤る）。返り値: 処理したか
     */
    public boolean handleBuyClick(Player player, int slot) {
        if (slot < 0 || slot >= 45 || slot >= stock.size()) {
            return false;
        }
        BlackMarketItem entry = stock.get(slot);
        var data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (!data.hasBalance(entry.getPrice())) {
            player.sendMessage(ChatColor.RED + "所持金が不足しています。");
            return true;
        }
        data.deductBalance(entry.getPrice());
        stock.remove(entry);
        player.getInventory().addItem(entry.getItem().clone()).values()
                .forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        player.sendMessage(ChatColor.GREEN + "購入しました: " + (entry.getItem().hasItemMeta() && entry.getItem().getItemMeta().hasDisplayName()
                ? entry.getItem().getItemMeta().getDisplayName() : entry.getItem().getType().name()));
        // 在庫が変わったので開き直して表示を更新する
        openMenu(player);
        return true;
    }

    private static class ItemRegistryProxy {
        static ItemStack empty() {
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "在庫がありません");
            item.setItemMeta(meta);
            return item;
        }
    }
}
