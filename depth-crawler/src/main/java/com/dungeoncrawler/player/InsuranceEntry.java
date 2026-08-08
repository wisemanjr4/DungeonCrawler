package com.dungeoncrawler.player;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InsuranceEntry {

    private final UUID entryId;
    private long expiry;
    private boolean claimed;
    private String provider;
    private ItemStack item;
    private String merchant;

    public InsuranceEntry(UUID entryId, long expiry, boolean claimed, String provider, ItemStack item, String merchant) {
        this.entryId = entryId;
        this.expiry = expiry;
        this.claimed = claimed;
        this.provider = provider;
        this.item = item;
        this.merchant = merchant;
    }

    public UUID getEntryId() {
        return entryId;
    }

    public long getExpiry() {
        return expiry;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public String getProvider() {
        return provider;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getMerchant() {
        return merchant;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiry;
    }

    public String serialize() {
        return entryId + "|" + expiry + "|" + claimed + "|" + provider + "|" + merchant + "|" + ItemSerializer.serialize(item);
    }

    public static InsuranceEntry deserialize(String data) {
        String[] parts = data.split("\\|", 6);
        if (parts.length < 6) {
            return null;
        }
        try {
            UUID id = UUID.fromString(parts[0]);
            long expiry = Long.parseLong(parts[1]);
            boolean claimed = Boolean.parseBoolean(parts[2]);
            String provider = parts[3];
            String merchant = parts[4];
            ItemStack item = ItemSerializer.deserialize(parts[5]);
            if (item == null) {
                return null;
            }
            return new InsuranceEntry(id, expiry, claimed, provider, item, merchant);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
