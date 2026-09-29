package com.dungeoncrawler.player;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private double balance;
    private int streak;
    private int highestFloor;
    private int totalRuns;
    private int successfulExtractions;
    private boolean mercyUsed;
    private boolean inDungeon;

    private final List<SafeBoxSlot> safeBoxSlots;
    private final List<InsuranceEntry> insurance;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.balance = 0;
        this.streak = 0;
        this.highestFloor = 0;
        this.totalRuns = 0;
        this.successfulExtractions = 0;
        this.mercyUsed = false;
        this.safeBoxSlots = new ArrayList<>();
        this.insurance = new ArrayList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = Math.max(0, balance);
    }

    public void addBalance(double amount) {
        this.balance = Math.max(0, this.balance + amount);
    }

    public boolean hasBalance(double amount) {
        return balance >= amount;
    }

    public boolean deductBalance(double amount) {
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        return true;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = Math.max(0, streak);
    }

    public int getHighestFloor() {
        return highestFloor;
    }

    public void setHighestFloor(int highestFloor) {
        this.highestFloor = Math.max(this.highestFloor, highestFloor);
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void incrementTotalRuns() {
        this.totalRuns++;
    }

    public int getSuccessfulExtractions() {
        return successfulExtractions;
    }

    public void incrementSuccessfulExtractions() {
        this.successfulExtractions++;
    }

    public boolean isMercyUsed() {
        return mercyUsed;
    }

    public boolean isInDungeon() {
        return inDungeon;
    }

    public void setInDungeon(boolean inDungeon) {
        this.inDungeon = inDungeon;
    }

    public void setMercyUsed(boolean mercyUsed) {
        this.mercyUsed = mercyUsed;
    }

    public List<SafeBoxSlot> getSafeBoxSlots() {
        return safeBoxSlots;
    }

    public List<InsuranceEntry> getInsurance() {
        return insurance;
    }

    public void toSection(ConfigurationSection section) {
        section.set("balance", balance);
        section.set("streak", streak);
        section.set("highest-floor", highestFloor);
        section.set("total-runs", totalRuns);
        section.set("successful-extractions", successfulExtractions);
        section.set("mercy-used", mercyUsed);
        section.set("in-dungeon", inDungeon);

        List<String> serializedBox = new ArrayList<>();
        for (SafeBoxSlot slot : safeBoxSlots) {
            serializedBox.add(slot.getSlot() + "|" + ItemSerializer.serialize(slot.getItem()));
        }
        section.set("safebox-slots", serializedBox);

        List<String> serializedInsurance = new ArrayList<>();
        for (InsuranceEntry entry : insurance) {
            serializedInsurance.add(entry.serialize());
        }
        section.set("insurance", serializedInsurance);
    }

    public static PlayerData fromSection(UUID uuid, ConfigurationSection section) {
        PlayerData data = new PlayerData(uuid);
        data.balance = section.getDouble("balance", 0);
        data.streak = section.getInt("streak", 0);
        data.highestFloor = section.getInt("highest-floor", 0);
        data.totalRuns = section.getInt("total-runs", 0);
        data.successfulExtractions = section.getInt("successful-extractions", 0);
        data.mercyUsed = section.getBoolean("mercy-used", false);
        data.inDungeon = section.getBoolean("in-dungeon", false);

        List<String> box = section.getStringList("safebox-slots");
        for (String entry : box) {
            int sep = entry.indexOf('|');
            if (sep < 0) {
                continue;
            }
            try {
                int slot = Integer.parseInt(entry.substring(0, sep));
                ItemStack item = ItemSerializer.deserialize(entry.substring(sep + 1));
                if (item != null) {
                    data.safeBoxSlots.add(new SafeBoxSlot(slot, item));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        List<String> ins = section.getStringList("insurance");
        for (String entry : ins) {
            InsuranceEntry parsed = InsuranceEntry.deserialize(entry);
            if (parsed != null) {
                data.insurance.add(parsed);
            }
        }
        return data;
    }

    public static class SafeBoxSlot {
        private final int slot;
        private final ItemStack item;

        public SafeBoxSlot(int slot, ItemStack item) {
            this.slot = slot;
            this.item = item;
        }

        public int getSlot() {
            return slot;
        }

        public ItemStack getItem() {
            return item;
        }
    }
}
