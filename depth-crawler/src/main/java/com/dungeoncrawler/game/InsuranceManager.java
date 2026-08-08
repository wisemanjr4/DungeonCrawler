package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.data.PlayerDataManager;
import com.dungeoncrawler.player.InsuranceEntry;
import com.dungeoncrawler.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InsuranceManager {

    public enum Provider {
        ROYAL("§6王国保険組合", 0.50, 1.0, 6 * 60 * 60 * 1000L, "GOLD_BLOCK"),
        ADVENTURER("§e冒険者ギルド", 0.30, 1.0, 24 * 60 * 60 * 1000L, "IRON_BLOCK"),
        EXPRESS("§b早馬急便", 0.55, 0.85, 3 * 60 * 60 * 1000L, "DIAMOND"),
        PAWNSHOP("§7裏路地の質屋", 0.15, 0.70, 48 * 60 * 60 * 1000L, "DARK_OAK_LOG"),
        BLACKMARKET("§5闇市の仲介人", 0.08, 0.50, 72 * 60 * 60 * 1000L, "OBSIDIAN");

        private final String display;
        private final double feeRate;
        private final double returnRate;
        private final long returnTime;
        private final String icon;

        Provider(String display, double feeRate, double returnRate, long returnTime, String icon) {
            this.display = display;
            this.feeRate = feeRate;
            this.returnRate = returnRate;
            this.returnTime = returnTime;
            this.icon = icon;
        }

        public String getDisplay() {
            return display;
        }

        public double getFeeRate() {
            return feeRate;
        }

        public double getReturnRate() {
            return returnRate;
        }

        public long getReturnTime() {
            return returnTime;
        }

        public String getIcon() {
            return icon;
        }
    }

    private final DepthCrawlerPlugin plugin;

    public InsuranceManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 保険契約。料金はアイテム価値×料金率。返り値: 成功可否
     */
    public boolean insure(Player player, ItemStack item, Provider provider) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        double value = plugin.getItemRegistry().getValue(item);
        double fee = value * provider.getFeeRate();
        if (!data.hasBalance(fee)) {
            player.sendMessage("§c保険料が不足しています（必要: " + (int) fee + "G）");
            return false;
        }
        data.deductBalance(fee);
        plugin.getItemRegistry().markInsured(item);
        InsuranceEntry entry = new InsuranceEntry(UUID.randomUUID(),
                System.currentTimeMillis() + provider.getReturnTime(), false,
                provider.name(), item.clone(), provider.getDisplay());
        data.getInsurance().add(entry);
        player.sendMessage("§a保険契約しました: " + provider.getDisplay() + "（" + (int) fee + "G）");
        return true;
    }

    public List<InsuranceEntry> getClaimable(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        List<InsuranceEntry> result = new ArrayList<>();
        for (InsuranceEntry entry : data.getInsurance()) {
            if (!entry.isClaimed() && entry.isExpired()) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * 全武器/防具を一括保険。返り値: 成功数
     */
    public int insureAllEquipment(Player player, Provider provider) {
        int count = 0;
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        List<ItemStack> targets = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            if (plugin.getItemRegistry().isEquipment(item) && !plugin.getItemRegistry().isInsured(item)) {
                targets.add(item);
            }
        }
        double totalFee = 0;
        for (ItemStack item : targets) {
            double value = plugin.getItemRegistry().getValue(item);
            totalFee += value * provider.getFeeRate();
        }
        if (!data.hasBalance(totalFee)) {
            player.sendMessage("§c保険料が不足しています（必要: " + (int) totalFee + "G）");
            return 0;
        }
        data.deductBalance(totalFee);
        for (ItemStack item : targets) {
            plugin.getItemRegistry().markInsured(item);
            InsuranceEntry entry = new InsuranceEntry(UUID.randomUUID(),
                    System.currentTimeMillis() + provider.getReturnTime(), false,
                    provider.name(), item.clone(), provider.getDisplay());
            data.getInsurance().add(entry);
            count++;
        }
        player.sendMessage("§a全装備 " + count + " 件を一括保険しました（" + (int) totalFee + "G）。");
        return count;
    }

    /**
     * 死亡時に保険アイテムを退避し、インベントリを全消去。
     * 保険アイテムは契約時のエントリ（クローン済み）で回収可能。未保険は全ロスト。
     */
    public void evacuateOnDeath(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        int evacuated = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            if (plugin.getItemRegistry().isInsured(item)) {
                evacuated++;
            } else {
                // 未保険アイテムはブラックマーケットへ流出（50%）
                plugin.getBlackMarketManager().addOnDeath(player, item);
            }
        }
        player.getInventory().clear();
        if (evacuated > 0) {
            player.sendMessage("§a保険アイテムを " + evacuated + " 件退避しました。返還時間経過後に /dungeon claim で回収できます。");
        }
    }

    /**
     * 保険回収（返還率ロール）。
     */
    public boolean claim(Player player, InsuranceEntry entry) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        Provider provider;
        try {
            provider = Provider.valueOf(entry.getProvider());
        } catch (IllegalArgumentException e) {
            provider = Provider.ADVENTURER;
        }
        entry.setClaimed(true);
        data.getInsurance().remove(entry);
        if (Math.random() <= provider.getReturnRate()) {
            player.getInventory().addItem(entry.getItem());
            player.sendMessage("§a保険アイテムを回収しました。");
            return true;
        }
        player.sendMessage("§c保険アイテムの返還に失敗しました...");
        return false;
    }

    public int getInsuranceCount(Player player) {
        return plugin.getPlayerDataManager().get(player.getUniqueId()).getInsurance().size();
    }
}
