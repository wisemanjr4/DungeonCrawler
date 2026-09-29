package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.item.ItemGenerationResult;
import com.dungeoncrawler.item.ItemNameGenerator;
import com.dungeoncrawler.item.ItemRegistry;
import com.dungeoncrawler.item.NameStats;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class LootManager {

    private final DepthCrawlerPlugin plugin;
    private final Random random = new Random();

    private static final Material[] MATERIAL_DROPS = {
            Material.BLAZE_ROD, Material.ENDER_PEARL, Material.GHAST_TEAR, Material.BONE,
            Material.SPIDER_EYE, Material.GUNPOWDER, Material.ROTTEN_FLESH, Material.STRING,
            Material.QUARTZ, Material.REDSTONE, Material.LAPIS_LAZULI, Material.AMETHYST_SHARD,
            Material.ECHO_SHARD, Material.PRISMARINE_SHARD, Material.PHANTOM_MEMBRANE,
            Material.SHULKER_SHELL, Material.NETHER_STAR, Material.DIAMOND, Material.EMERALD,
            Material.NETHERITE_SCRAP
    };

    private static final Material[] VALUABLE_DROPS = {
            Material.GOLD_INGOT, Material.IRON_INGOT, Material.COAL, Material.DIAMOND,
            Material.EMERALD, Material.LAPIS_LAZULI, Material.QUARTZ, Material.AMETHYST_SHARD,
            Material.REDSTONE, Material.GOLD_NUGGET, Material.NAUTILUS_SHELL,
            Material.PRISMARINE_CRYSTALS, Material.SLIME_BALL, Material.HONEY_BOTTLE,
            Material.SCUTE, Material.TURTLE_EGG, Material.EXPERIENCE_BOTTLE
    };

    private static final String[] MATERIAL_NAMES = {
            "深層の核", "竜の鱗片", "螺環の破片", "魔力の結晶", "古代の遺物", "歪んだ鉄",
            "呪われた欠片", "聖者の歯車", "暗黒の心臓", "炎の種", "氷の花", "雷の羽毛",
            "毒の腺", "影の絹", "星の砂", "月の滴", "風の結晶", "大地の核", "時空の欠片",
            "魂の結晶", "混沌のしずく", "秩序の断片", "巨獣の牙", "精霊の欠片", "夢の残滓",
            "魔女の香", "竜の血", "天使の羽根", "堕天使の爪", "魔王の角", "古代の歯車",
            "永劫の破片", "螺旋の破片", "鉄の塊", "鉱石の結晶", "黄昏の塵", "黎明の水滴",
            "契約の欠片", "禁断の書の断章", "原初の魔力"
    };

    private static final String[] VALUABLE_NAMES = {
            "古代の貨", "深層の真珠", "魔王の指輪", "幻の銀貨", "禁断の宝石", "王家の印章",
            "竜の目玉", "星のロザリオ", "月の聖杯", "黄金の彫像", "暗黒の宝珠", "天使の胸像",
            "海王の貝殻", "風の宝飾", "雷の宝玉", "氷の彫刻", "炎のタリスマン", "毒のアミュレット",
            "時計台の歯車", "迷宮の鍵", "古の地図", "魔力の瓶", "召喚の指輪", "封筒",
            "王家の血統証", "錆びた勲章", "海賊の宝箱", "魔導の石版", "祝福のチョーカー",
            "呪いの首飾り"
    };

    public LootManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 通常モブのドロップ（35%で素材 or 換金品）。
     */
    public List<ItemStack> rollMobDrop(int floor) {
        List<ItemStack> drops = new ArrayList<>();
        double roll = random.nextDouble();
        if (roll < 0.35) {
            drops.add(rollMaterial(floor));
        } else if (roll < 0.60) {
            drops.add(rollValuable(floor));
        }
        return drops;
    }

    /**
     * エリートモブのドロップ（素材 + 換金品 を保証、50%で装備）。
     */
    public List<ItemStack> rollEliteDrop(int floor) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(rollMaterial(floor));
        drops.add(rollValuable(floor));
        if (random.nextDouble() < 0.5) {
            drops.add(random.nextBoolean() ? rollWeapon(floor, 0.1) : rollArmor(floor, 0.1));
        }
        return drops;
    }

    /**
     * BOSSドロップ（仕様7.1: ネームド武器/防具 + 100〜500G）。
     */
    public List<ItemStack> rollBossDrop(int floor) {
        List<ItemStack> drops = new ArrayList<>();
        boolean weapon = random.nextBoolean();
        ItemStack named = weapon ? rollWeapon(floor, 0.2) : rollArmor(floor, 0.2);
        drops.add(named);
        int gold = 100 + random.nextInt(401);
        ItemStack goldItem = ItemRegistry.createSimple("§6金貨", Material.GOLD_INGOT, "§6", Math.max(1, gold / 10));
        plugin.getItemRegistry().setValue(goldItem, gold);
        drops.add(goldItem);
        return drops;
    }

    public List<ItemStack> rollLoot(int floor, double dropMult, double qualityBonus) {
        List<ItemStack> drops = new ArrayList<>();
        double weaponChance = 0.35 * dropMult;
        double armorChance = 0.20 * dropMult;
        double materialChance = 0.30 * dropMult;
        double valuableChance = 0.25 * dropMult;

        if (random.nextDouble() < weaponChance) {
            drops.add(rollWeapon(floor, qualityBonus));
        }
        if (random.nextDouble() < armorChance) {
            drops.add(rollArmor(floor, qualityBonus));
        }
        if (random.nextDouble() < materialChance) {
            drops.add(rollMaterial(floor));
        }
        if (random.nextDouble() < valuableChance) {
            drops.add(rollValuable(floor));
        }
        return drops;
    }

    public ItemStack rollWeapon(int floor, double qualityBonus) {
        int tier = tierForFloor(floor);
        Material mat = weaponMaterial(tier);
        ItemStack weapon = ItemRegistry.createSimple("武器", mat, "", 1);
        ItemRegistry.makeUnbreakable(weapon);
        ItemMeta meta = weapon.getItemMeta();
        meta.getPersistentDataContainer().set(ItemRegistry.IS_EQUIPMENT, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(ItemRegistry.TIER, org.bukkit.persistence.PersistentDataType.INTEGER, tier);
        weapon.setItemMeta(meta);

        // ネームド（30%）は固定ボーナス、通常は品質アフィックス補正
        ItemGenerationResult gen = random.nextDouble() < 0.30
                ? plugin.getItemNameGenerator().generateNamedWeapon()
                : plugin.getItemNameGenerator().generateWeapon();
        String name = gen.getName();

        String rarity = rollRarity(qualityBonus);
        double[] stats = statsForTier(tier);
        double rarityFactor = rarityFactor(rarity);
        double atk = stats[0] * rarityFactor * gen.getAtkMult();
        double def = stats[1] * rarityFactor * gen.getDefMult();
        double spd = stats[2] * rarityFactor * gen.getSpdMult();
        double crit = stats[3] + rarityCrit(rarity) + gen.getCritBonus();
        double hp = stats[4] * rarityFactor * gen.getHpMult();
        double ls = stats[5] + rarityLifesteal(rarity) + gen.getLifestealBonus();

        NameStats nameStats = new NameStats(UUID.randomUUID(), atk, def, spd, crit, hp, ls);
        plugin.getItemRegistry().attachStats(weapon, nameStats, rarity);

        String color = rarityColor(rarity);
        meta = weapon.getItemMeta();
        meta.setDisplayName(color + name);
        List<String> lore = new ArrayList<>();
        lore.add("§7等級: " + color + rarity);
        if (gen.isNamed()) {
            lore.add("§6§lネームド");
        }
        lore.add("§7攻撃力: §c" + format(atk));
        lore.add("§7防御力: §7" + format(def));
        lore.add("§7速度: §b" + format(spd));
        lore.add("§7クリティカル: §d" + format(crit) + "%");
        lore.add("§7HP倍率: §e" + format(hp));
        if (ls > 0) {
            lore.add("§7吸血: §c" + format(ls) + "%");
        }
        double value = valueForRarity(rarity);
        if (gen.isNamed()) {
            value *= 1.5;
        }
        meta.setLore(lore);
        weapon.setItemMeta(meta);
        plugin.getItemRegistry().setValue(weapon, value);
        return weapon;
    }

    public ItemStack rollArmor(int floor, double qualityBonus) {
        int tier = tierForFloor(floor);
        // 名前を先に生成し、部位（兜/ブーツ等）と材質をティアに一致させる
        ItemGenerationResult gen = random.nextDouble() < 0.08
                ? plugin.getItemNameGenerator().generateNamedArmor()
                : plugin.getItemNameGenerator().generateArmor(armorMaterialPrefix(tier));
        Material mat = armorMaterial(tier, armorSlot(gen.getName()));
        ItemStack armor = ItemRegistry.createSimple("防具", mat, "", 1);
        ItemRegistry.makeUnbreakable(armor);
        ItemMeta meta = armor.getItemMeta();
        meta.getPersistentDataContainer().set(ItemRegistry.IS_EQUIPMENT, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(ItemRegistry.TIER, org.bukkit.persistence.PersistentDataType.INTEGER, tier);
        armor.setItemMeta(meta);

        String name = gen.getName();
        String rarity = rollRarity(qualityBonus);
        double[] stats = statsForTier(tier);
        double rarityFactor = rarityFactor(rarity);
        double atk = stats[0] * rarityFactor * gen.getAtkMult();
        double def = stats[1] * rarityFactor * gen.getDefMult();
        double spd = stats[2] * rarityFactor * gen.getSpdMult();
        double crit = stats[3] + rarityCrit(rarity) + gen.getCritBonus();
        double hp = stats[4] * rarityFactor * gen.getHpMult();
        double ls = stats[5] + rarityLifesteal(rarity) + gen.getLifestealBonus();

        NameStats nameStats = new NameStats(UUID.randomUUID(), atk, def, spd, crit, hp, ls);
        plugin.getItemRegistry().attachStats(armor, nameStats, rarity);

        String color = rarityColor(rarity);
        meta = armor.getItemMeta();
        meta.setDisplayName(color + name);
        List<String> lore = new ArrayList<>();
        lore.add("§7等級: " + color + rarity);
        if (gen.isNamed()) {
            lore.add("§6§lネームド");
        }
        lore.add("§7攻撃力: §c" + format(atk));
        lore.add("§7防御力: §7" + format(def));
        lore.add("§7速度: §b" + format(spd));
        lore.add("§7クリティカル: §d" + format(crit) + "%");
        lore.add("§7HP倍率: §e" + format(hp));
        if (ls > 0) {
            lore.add("§7吸血: §c" + format(ls) + "%");
        }
        double value = valueForRarity(rarity);
        if (gen.isNamed()) {
            value *= 1.5;
        }
        meta.setLore(lore);
        armor.setItemMeta(meta);
        plugin.getItemRegistry().setValue(armor, value);
        return armor;
    }

    public ItemStack rollMaterial(int floor) {
        String name = MATERIAL_NAMES[random.nextInt(MATERIAL_NAMES.length)];
        Material mat = MATERIAL_DROPS[random.nextInt(MATERIAL_DROPS.length)];
        int amount = 1 + random.nextInt(3);
        ItemStack item = ItemRegistry.createSimple(name, mat, "§e", amount);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemRegistry.IS_DC_ITEM, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(ItemRegistry.IS_MATERIAL, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(ItemRegistry.ITEM_RARITY, org.bukkit.persistence.PersistentDataType.STRING, "MATERIAL");
        meta.setLore(List.of("§7ダンジョン素材。売却または強化に使用。"));
        item.setItemMeta(meta);
        plugin.getItemRegistry().setValue(item, 10 + floor * 2);
        return item;
    }

    public ItemStack rollValuable(int floor) {
        String name = VALUABLE_NAMES[random.nextInt(VALUABLE_NAMES.length)];
        Material mat = VALUABLE_DROPS[random.nextInt(VALUABLE_DROPS.length)];
        ItemStack item = ItemRegistry.createSimple(name, mat, "§6", 1);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemRegistry.IS_DC_ITEM, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(ItemRegistry.ITEM_RARITY, org.bukkit.persistence.PersistentDataType.STRING, "VALUABLE");
        meta.setLore(List.of("§7換金用アイテム。"));
        item.setItemMeta(meta);
        plugin.getItemRegistry().setValue(item, 25 + floor * 5);
        return item;
    }

    private int tierForFloor(int floor) {
        if (floor >= 50) return 6;
        if (floor >= 30) return 5;
        if (floor >= 20) return 4;
        if (floor >= 10) return 3;
        return 1 + (floor / 5);
    }

    private Material weaponMaterial(int tier) {
        return switch (tier) {
            case 1 -> Material.WOODEN_SWORD;
            case 2 -> Material.STONE_SWORD;
            case 3 -> Material.IRON_SWORD;
            case 4 -> Material.GOLDEN_SWORD;
            case 5 -> Material.DIAMOND_SWORD;
            default -> Material.NETHERITE_SWORD;
        };
    }

    private String armorMaterialPrefix(int tier) {
        return switch (tier) {
            case 1 -> "革の";
            case 2 -> "鎖の";
            case 3 -> "鉄の";
            case 4 -> "黄金の";
            case 5 -> "ダイヤの";
            default -> "ネザライトの";
        };
    }

    /** ベース名から装備部位を推定: 0=頭 1=胴 2=脚 3=足 */
    private int armorSlot(String name) {
        String[] head = {"兜", "フード", "ヘルメット", "ヘルム", "サークレット", "ティアラ", "王冠", "マスク",
                "ヴェール", "ハット", "ボンネット", "ヘッドピース", "リボン"};
        String[] legs = {"レッグガード", "ジュポン", "スカート", "ベルト", "サッシュ", "ロンメル"};
        String[] feet = {"グリーブ", "ブーツ", "シューズ", "サンダル", "ソレア", "ウィンギャード"};
        for (String k : head) if (name.contains(k)) return 0;
        for (String k : feet) if (name.contains(k)) return 3;
        for (String k : legs) if (name.contains(k)) return 2;
        return 1;
    }

    private Material armorMaterial(int tier, int slot) {
        String prefix = switch (tier) {
            case 1 -> "LEATHER";
            case 2 -> "CHAINMAIL";
            case 3 -> "IRON";
            case 4 -> "GOLDEN";
            case 5 -> "DIAMOND";
            default -> "NETHERITE";
        };
        String part = switch (slot) {
            case 0 -> "HELMET";
            case 2 -> "LEGGINGS";
            case 3 -> "BOOTS";
            default -> "CHESTPLATE";
        };
        return Material.valueOf(prefix + "_" + part);
    }

    private double[] statsForTier(int tier) {
        // [ATK, DEF, SPD, CRIT, HP, LS]
        return switch (tier) {
            case 1 -> new double[]{1.5, 1.5, 1.0, 5, 1.0, 0};
            case 2 -> new double[]{4, 4.5, 1.0, 5, 1.0, 0};
            case 3 -> new double[]{7, 7.5, 1.0, 5, 1.0, 0};
            case 4 -> new double[]{10, 10.5, 1.0, 5, 1.0, 0};
            case 5 -> new double[]{13, 14.5, 1.0, 5, 1.0, 0};
            default -> new double[]{18.5, 20.5, 1.0, 5, 1.0, 0};
        };
    }

    private String rollRarity(double qualityBonus) {
        double roll = random.nextDouble();
        if (roll < 0.40 - qualityBonus * 0.1) {
            return "COMMON";
        } else if (roll < 0.70 - qualityBonus * 0.1) {
            return "UNCOMMON";
        } else if (roll < 0.88 - qualityBonus * 0.05) {
            return "RARE";
        } else if (roll < 0.96) {
            return "EPIC";
        }
        return "LEGENDARY";
    }

    private double rarityFactor(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> 1.15;
            case "RARE" -> 1.30;
            case "EPIC" -> 1.50;
            case "LEGENDARY" -> 1.80;
            default -> 1.0;
        };
    }

    private double rarityCrit(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> 2;
            case "RARE" -> 4;
            case "EPIC" -> 7;
            case "LEGENDARY" -> 10;
            default -> 0;
        };
    }

    private double rarityLifesteal(String rarity) {
        return switch (rarity) {
            case "EPIC" -> 2;
            case "LEGENDARY" -> 4;
            default -> 0;
        };
    }

    private double valueForRarity(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> 40;
            case "RARE" -> 75;
            case "EPIC" -> 160;
            case "LEGENDARY" -> 350;
            default -> 12;
        };
    }

    private String rarityColor(String rarity) {
        return switch (rarity) {
            case "UNCOMMON" -> "§a";
            case "RARE" -> "§b";
            case "EPIC" -> "§d";
            case "LEGENDARY" -> "§6";
            default -> "§7";
        };
    }

    private String format(double v) {
        return String.format("%.1f", v);
    }
}
