package com.dungeoncrawler.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShopItem {

    private final String name;
    private final ItemStack item;
    private final double price;

    public ShopItem(String name, ItemStack item, double price) {
        this.name = name;
        this.item = item;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public static ShopItem of(String name, Material material, int amount, double price) {
        ItemStack item = new ItemStack(material, amount);
        // 表示名がないと購入クリック時に名前で照合できない
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + name);
        item.setItemMeta(meta);
        return new ShopItem(name, item, price);
    }
}
