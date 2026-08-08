package com.dungeoncrawler.shop;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.item.ItemRegistry;
import com.dungeoncrawler.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {

    public static final String SHOP_TITLE = "§6§lショップ";
    public static final String BUY_TITLE = "§6§l購入メニュー";
    public static final String SELL_TITLE = "§e§l売却メニュー";

    private final DepthCrawlerPlugin plugin;
    private final List<ShopItem> shopItems;

    public ShopManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.shopItems = new ArrayList<>();
        initShop();
    }

    private void initShop() {
        shopItems.add(ShopItem.of("リペアキット", Material.SHEARS, 1, 100));
        shopItems.add(ShopItem.of("撤退コンパス", Material.COMPASS, 1, 50));
        shopItems.add(ShopItem.of("バックパック", Material.CHEST, 1, 200));
        shopItems.add(ShopItem.of("食料パック", Material.BREAD, 4, 20));
        shopItems.add(ShopItem.of("力のポーション", Material.POTION, 1, 60));
        shopItems.add(ShopItem.of("回復ポーション", Material.HONEY_BOTTLE, 1, 40));
        shopItems.add(ShopItem.of("鋼鉄の素材", Material.IRON_INGOT, 4, 30));
        shopItems.add(ShopItem.of("金の素材", Material.GOLD_INGOT, 2, 35));
        shopItems.add(ShopItem.of("ダイヤの素材", Material.DIAMOND, 1, 80));
        shopItems.add(ShopItem.of("精密鍛石", Material.LAPIS_LAZULI, 1, 150));
    }

    public void openMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, SHOP_TITLE);
        ItemStack buy = new ItemStack(Material.EMERALD);
        ItemMeta buyMeta = buy.getItemMeta();
        buyMeta.setDisplayName(ChatColor.GREEN + "購入");
        buy.setItemMeta(buyMeta);
        gui.setItem(2, buy);

        ItemStack sell = new ItemStack(Material.GOLD_INGOT);
        ItemMeta sellMeta = sell.getItemMeta();
        sellMeta.setDisplayName(ChatColor.GOLD + "売却");
        sell.setItemMeta(sellMeta);
        gui.setItem(6, sell);
        player.openInventory(gui);
    }

    public void openBuyMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, BUY_TITLE);
        int slot = 0;
        for (ShopItem item : shopItems) {
            if (slot >= 45) {
                break;
            }
            ItemStack display = item.getItem().clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
            lore.add(ChatColor.GOLD + "価格: " + (int) item.getPrice() + "G");
            meta.setLore(lore);
            display.setItemMeta(meta);
            gui.setItem(slot, display);
            slot++;
        }
        player.openInventory(gui);
    }

    public void openSellMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, SELL_TITLE);
        // 右下に売却ボタン
        ItemStack sellAll = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = sellAll.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "一括売却");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "インベントリ内の素材・換金品を売却します。");
        meta.setLore(lore);
        sellAll.setItemMeta(meta);
        gui.setItem(44, sellAll);
        player.openInventory(gui);
    }

    /**
     * ショップGUI内クリック処理。返り値: 処理したか
     */
    public boolean handleBuyClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) {
            return false;
        }
        String stripped = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        for (ShopItem item : shopItems) {
            if (item.getItem().getType() == clicked.getType()
                    && stripped != null && stripped.equals(item.getName())) {
                PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
                if (!data.hasBalance(item.getPrice())) {
                    player.sendMessage("§c所持金が不足しています。");
                    return true;
                }
                data.deductBalance(item.getPrice());
                ItemStack toGive = item.getItem().clone();
                if (item.getItem().getType() == Material.POTION) {
                    org.bukkit.inventory.meta.PotionMeta potionMeta = (org.bukkit.inventory.meta.PotionMeta) toGive.getItemMeta();
                    potionMeta.setBasePotionData(new org.bukkit.potion.PotionData(org.bukkit.potion.PotionType.STRENGTH));
                    toGive.setItemMeta(potionMeta);
                }
                markConsumable(toGive, item.getName());
                player.getInventory().addItem(toGive);
                player.sendMessage(ChatColor.GREEN + "購入しました: " + item.getName());
                return true;
            }
        }
        return false;
    }

    /**
     * 消費アイテムに種別をマーク（仕様7.3）。
     * type: REPAIR_KIT / BACKPACK / COMPASS / FOOD / POTION
     */
    private void markConsumable(ItemStack item, String name) {
        String type = switch (name) {
            case "リペアキット" -> "REPAIR_KIT";
            case "バックパック" -> "BACKPACK";
            case "撤退コンパス" -> "COMPASS";
            case "食料パック" -> "FOOD";
            case "力のポーション", "回復ポーション" -> "POTION";
            default -> null;
        };
        if (type != null) {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(ItemRegistry.CONSUMABLE, org.bukkit.persistence.PersistentDataType.STRING, type);
            item.setItemMeta(meta);
        }
    }

    /**
     * インベントリ内の売却可能アイテムを売却。返り値: 売却額
     */
    public double sellAllSellables(Player player) {
        double total = 0;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) {
                continue;
            }
            double value = plugin.getItemRegistry().getValue(item);
            if (value > 0) {
                total += value * item.getAmount();
                player.getInventory().setItem(i, null);
            }
        }
        if (total > 0) {
            plugin.getPlayerDataManager().get(player.getUniqueId()).addBalance(total);
            player.sendMessage(ChatColor.GOLD + "売却完了: " + (int) total + "G");
        } else {
            player.sendMessage(ChatColor.GRAY + "売却できるアイテムがありません。");
        }
        return total;
    }
}
