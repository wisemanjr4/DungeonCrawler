package com.dungeoncrawler.player;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.player.PlayerData.SafeBoxSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SafeBoxManager {

    public static final String TITLE = "§8セーフティボックス";

    private final DepthCrawlerPlugin plugin;
    private final Map<UUID, Integer> openPage = new HashMap<>();

    public SafeBoxManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        int maxPages = plugin.getDungeonConfig().getMaxSafeBoxPages();
        page = Math.max(1, Math.min(maxPages, page));
        openPage.put(player.getUniqueId(), page);

        Inventory gui = Bukkit.createInventory(null, 54, TITLE + " §7(" + page + "/" + maxPages + ")");
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());

        for (SafeBoxSlot slot : data.getSafeBoxSlots()) {
            int slotIndex = slot.getSlot();
            if (pageOf(slotIndex) == page) {
                gui.setItem(slotIndex % 45, slot.getItem().clone());
            }
        }

        // ナビゲーションバー（下段9スロット）
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prev.getItemMeta();
        prevMeta.setDisplayName(ChatColor.GRAY + "前のページ");
        prev.setItemMeta(prevMeta);
        gui.setItem(45, prev);

        ItemStack deposit = new ItemStack(Material.CHEST);
        ItemMeta depositMeta = deposit.getItemMeta();
        depositMeta.setDisplayName(ChatColor.GOLD + "預入");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "手持ちのアイテムを預入します。");
        depositMeta.setLore(lore);
        deposit.setItemMeta(depositMeta);
        gui.setItem(48, deposit);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GRAY + "次のページ");
        next.setItemMeta(nextMeta);
        gui.setItem(53, next);

        player.openInventory(gui);
    }

    private int pageOf(int slot) {
        return slot / 45 + 1;
    }

    public int getOpenPage(Player player) {
        return openPage.getOrDefault(player.getUniqueId(), 1);
    }

    /**
     * セーフボックスに預入。手持ち（メインハンド）を預ける。
     */
    public boolean deposit(Player player) {
        return deposit(player, player.getInventory().getItemInMainHand(), player.getInventory().getHeldItemSlot());
    }

    /**
     * セーフボックスに預入。指定スロットのアイテムを預ける。
     */
    public boolean deposit(Player player, ItemStack item, int slot) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int maxSlots = plugin.getDungeonConfig().getMaxSafeBoxSlots();
        int maxPages = plugin.getDungeonConfig().getMaxSafeBoxPages();
        int capacity = maxPages * 45;

        // スタック可能なら結合
        for (SafeBoxSlot storedSlot : data.getSafeBoxSlots()) {
            ItemStack stored = storedSlot.getItem();
            if (stored != null && stored.isSimilar(item)
                    && stored.getAmount() + item.getAmount() <= stored.getMaxStackSize()) {
                stored.setAmount(stored.getAmount() + item.getAmount());
                player.getInventory().setItem(slot, null);
                return true;
            }
        }

        if (data.getSafeBoxSlots().size() >= Math.min(maxSlots, capacity)) {
            return false;
        }
        int freeSlot = firstFreeSlot(data);
        if (freeSlot < 0) {
            return false;
        }
        data.getSafeBoxSlots().add(new SafeBoxSlot(freeSlot, item.clone()));
        player.getInventory().setItem(slot, null);
        return true;
    }

    private int firstFreeSlot(PlayerData data) {
        int capacity = plugin.getDungeonConfig().getMaxSafeBoxPages() * 45;
        boolean[] used = new boolean[capacity];
        for (SafeBoxSlot slot : data.getSafeBoxSlots()) {
            if (slot.getSlot() >= 0 && slot.getSlot() < capacity) {
                used[slot.getSlot()] = true;
            }
        }
        for (int i = 0; i < capacity; i++) {
            if (!used[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * セーフボックスから取り出す。
     */
    public boolean withdraw(Player player, int slotIndex) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int absolute = (getOpenPage(player) - 1) * 45 + slotIndex;
        SafeBoxSlot found = null;
        for (SafeBoxSlot slot : data.getSafeBoxSlots()) {
            if (slot.getSlot() == absolute) {
                found = slot;
                break;
            }
        }
        if (found == null) {
            return false;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "インベントリが満杯です。");
            return true;
        }
        player.getInventory().addItem(found.getItem().clone());
        data.getSafeBoxSlots().remove(found);
        return true;
    }
}
