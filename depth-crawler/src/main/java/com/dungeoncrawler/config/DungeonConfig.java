package com.dungeoncrawler.config;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class DungeonConfig {

    private final DepthCrawlerPlugin plugin;
    private final FileConfiguration cfg;

    public DungeonConfig(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
    }

    public int getMinFloors() {
        return cfg.getInt("dungeon.min-floors", 3);
    }

    public int getMaxFloors() {
        return cfg.getInt("dungeon.max-floors", 8);
    }

    public int getMinRoomSize() {
        return cfg.getInt("dungeon.min-room-size", 15);
    }

    public int getMaxRoomSize() {
        return cfg.getInt("dungeon.max-room-size", 30);
    }

    public double getExtractionRushDuration() {
        return cfg.getDouble("dungeon.extraction-rush-duration", 60.0);
    }

    public double getChestStreakMultiplier() {
        return cfg.getDouble("dungeon.chest-streak-multiplier", 0.25);
    }

    public int getGridSize() {
        return cfg.getInt("dungeon.grid-size", 10);
    }

    public int getRoomInnerSize() {
        return cfg.getInt("dungeon.room-inner-size", 10);
    }

    public int getFloorSpacing() {
        return cfg.getInt("dungeon.floor-spacing", 121);
    }

    public int getBaseY() {
        return cfg.getInt("dungeon.base-y", 63);
    }

    public double getExitStandSeconds() {
        return cfg.getDouble("dungeon.exit-stand-seconds", 3.0);
    }

    // --- 難易度（config.yml の difficulty.*。未設定なら従来値） ---

    public int getBaseMobCount() {
        return cfg.getInt("difficulty.base-mob-count", 30);
    }

    public int getMobCountPerFloor() {
        return cfg.getInt("difficulty.mob-count-per-floor", 3);
    }

    public double getMobBaseHp() {
        return cfg.getDouble("difficulty.mob-base-hp", 20.0);
    }

    public double getMobHpPerFloor() {
        return cfg.getDouble("difficulty.mob-hp-per-floor", 4.0);
    }

    public double getMobBaseAttack() {
        return cfg.getDouble("difficulty.mob-base-attack", 5.0);
    }

    public double getBossHp() {
        return cfg.getDouble("difficulty.boss-hp", 200.0);
    }

    public double getEliteHpMultiplier() {
        return cfg.getDouble("difficulty.elite-hp-multiplier", 2.5);
    }

    public double getEliteAttackMultiplier() {
        return cfg.getDouble("difficulty.elite-attack-multiplier", 1.4);
    }

    public int getExtractionRushMobs() {
        return cfg.getInt("difficulty.extraction-rush-mobs", 6);
    }

    /** 動的湧き: 2秒ごとに 1/N の確率で1体。大きいほど湧きにくい。 */
    public int getDynamicSpawnOdds() {
        return Math.max(1, cfg.getInt("difficulty.dynamic-spawn-odds", 15));
    }

    public int getMaxSafeBoxSlots() {
        return cfg.getInt("player.max-safe-box-slots", 54);
    }

    public int getMaxSafeBoxPages() {
        return cfg.getInt("player.max-safe-box-pages", 5);
    }

    public double getReforgeCost() {
        return cfg.getDouble("shop.reforge-cost", 100.0);
    }

    public double getHealCost() {
        return cfg.getDouble("shop.heal-cost", 50.0);
    }

    public double getInsuranceMerchantFeeRate() {
        return cfg.getDouble("insurance.merchant-fee-rate", 0.3);
    }

    public boolean isMobGriefingEnabled() {
        return cfg.getBoolean("world.mob-griefing", false);
    }

    public boolean isFireTickEnabled() {
        return cfg.getBoolean("world.do-fire-tick", false);
    }

    public int roomSizeForGrid() {
        // グリッドサイズ + 壁1ブロック → 迷路全体サイズ
        int grid = getGridSize();
        int inner = getRoomInnerSize();
        return grid * (inner + 1) + 1;
    }
}
