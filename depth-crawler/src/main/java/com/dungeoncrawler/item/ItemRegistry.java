package com.dungeoncrawler.item;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemRegistry {

    public static final NamespacedKey ATK_MULT = key("atk_mult");
    public static final NamespacedKey DEF_MULT = key("def_mult");
    public static final NamespacedKey SPD_MULT = key("spd_mult");
    public static final NamespacedKey CRIT_BONUS = key("crit_bonus");
    public static final NamespacedKey HP_MULT = key("hp_mult");
    public static final NamespacedKey LIFESTEAL = key("lifesteal");
    public static final NamespacedKey ITEM_ID = key("item_id");
    public static final NamespacedKey ITEM_RARITY = key("item_rarity");
    public static final NamespacedKey IS_DC_ITEM = key("is_dc_item");
    public static final NamespacedKey ITEM_KIND = key("item_kind");
    public static final NamespacedKey TIER = key("tier");
    public static final NamespacedKey IS_EQUIPMENT = key("is_equipment");
    public static final NamespacedKey IS_MATERIAL = key("is_material");
    public static final NamespacedKey VALUE = key("value");
    public static final NamespacedKey INSURED = key("insured");
    public static final NamespacedKey CONSUMABLE = key("consumable");

    private final DepthCrawlerPlugin plugin;

    public ItemRegistry(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    public static NamespacedKey key(String name) {
        return new NamespacedKey(DepthCrawlerPlugin.getInstance(), name);
    }

    public boolean isDcItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        Integer isDc = container.get(IS_DC_ITEM, PersistentDataType.INTEGER);
        return isDc != null && isDc == 1;
    }

    public String getRarity(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return "COMMON";
        }
        String rarity = item.getItemMeta().getPersistentDataContainer().get(ITEM_RARITY, PersistentDataType.STRING);
        return rarity == null ? "COMMON" : rarity;
    }

    public double getValue(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        Double value = item.getItemMeta().getPersistentDataContainer().get(VALUE, PersistentDataType.DOUBLE);
        return value == null ? 0 : value;
    }

    public void setValue(ItemStack item, double value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(VALUE, PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
    }

    public void markInsured(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(INSURED, PersistentDataType.INTEGER, 1);
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        lore.add("§6§l【ROYAL 保障済】");
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public boolean isInsured(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        Integer v = item.getItemMeta().getPersistentDataContainer().get(INSURED, PersistentDataType.INTEGER);
        return v != null && v == 1;
    }

    public void attachStats(ItemStack item, NameStats stats, String rarity) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer c = meta.getPersistentDataContainer();
        c.set(IS_DC_ITEM, PersistentDataType.INTEGER, 1);
        c.set(ITEM_ID, PersistentDataType.STRING, stats.getItemId().toString());
        c.set(ITEM_RARITY, PersistentDataType.STRING, rarity);
        stats.save(c);
        item.setItemMeta(meta);
    }

    public boolean isEquipment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        Integer v = item.getItemMeta().getPersistentDataContainer().get(IS_EQUIPMENT, PersistentDataType.INTEGER);
        return v != null && v == 1;
    }

    public boolean isMaterial(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        Integer v = item.getItemMeta().getPersistentDataContainer().get(IS_MATERIAL, PersistentDataType.INTEGER);
        return v != null && v == 1;
    }

    public static ItemStack createSimple(String displayName, Material material, String rarityColor, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(rarityColor + displayName);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeUnbreakable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static void addEnchantGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}
