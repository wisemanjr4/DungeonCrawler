package com.dungeoncrawler.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ItemNameGenerator {

    private static final Random RANDOM = new Random();

    private static final String[] QUALITY_MODIFIERS = {
            "伝説の", "神話の", "神威の", "破壊の", "狂気の", "不滅の", "疾風の", "無慈悲な",
            "精霊の", "呪われた", "聖なる", "業火の", "絶望の", "極光の", "紅蓮の", "幻影の",
            "混沌の", "虚空の", "禁忌の", "賢者の"
    };

    /**
     * 品質修飾子ごとのステータス補正（仕様書 6.3）。
     * 倍率系は1.0基準、加算系（crit/lifesteal）は0基準。
     */
    private static final Map<String, ItemGenerationResult> QUALITY_STATS = new HashMap<>();

    static {
        QUALITY_STATS.put("伝説の", new ItemGenerationResult("", 1.20, 1.0, 1.10, 0.15, 1.05, 0.0, false));
        QUALITY_STATS.put("破壊の", new ItemGenerationResult("", 1.25, 1.0, 0.95, 0.05, 1.0, 0.0, false));
        QUALITY_STATS.put("狂気の", new ItemGenerationResult("", 1.10, 0.90, 1.0, 0.20, 1.0, 0.0, false));
        QUALITY_STATS.put("不滅の", new ItemGenerationResult("", 0.95, 1.10, 1.0, 0.0, 1.20, 0.0, false));
        QUALITY_STATS.put("疾風の", new ItemGenerationResult("", 1.05, 1.0, 1.20, 0.0, 1.0, 0.0, false));
        QUALITY_STATS.put("無慈悲な", new ItemGenerationResult("", 1.20, 1.0, 1.0, 0.05, 1.0, 0.03, false));
        QUALITY_STATS.put("神話の", new ItemGenerationResult("", 1.30, 1.0, 1.0, 0.10, 1.10, 0.0, false));
        QUALITY_STATS.put("神威の", new ItemGenerationResult("", 1.15, 1.05, 1.05, 0.10, 1.0, 0.0, false));
        QUALITY_STATS.put("精霊の", new ItemGenerationResult("", 1.10, 1.10, 1.0, 0.05, 1.10, 0.0, false));
        QUALITY_STATS.put("呪われた", new ItemGenerationResult("", 1.35, 0.95, 1.0, 0.10, 0.90, 0.0, false));
        QUALITY_STATS.put("聖なる", new ItemGenerationResult("", 1.0, 1.20, 1.0, 0.0, 1.15, 0.0, false));
        QUALITY_STATS.put("業火の", new ItemGenerationResult("", 1.25, 0.95, 1.0, 0.10, 1.0, 0.0, false));
        QUALITY_STATS.put("絶望の", new ItemGenerationResult("", 1.15, 1.0, 0.95, 0.15, 1.0, 0.0, false));
        QUALITY_STATS.put("極光の", new ItemGenerationResult("", 1.10, 1.0, 1.10, 0.10, 1.05, 0.0, false));
        QUALITY_STATS.put("紅蓮の", new ItemGenerationResult("", 1.15, 0.95, 1.0, 0.15, 1.0, 0.0, false));
        QUALITY_STATS.put("幻影の", new ItemGenerationResult("", 1.0, 0.95, 1.25, 0.10, 1.0, 0.0, false));
        QUALITY_STATS.put("混沌の", new ItemGenerationResult("", 1.20, 1.0, 1.0, 0.10, 0.95, 0.0, false));
        QUALITY_STATS.put("虚空の", new ItemGenerationResult("", 1.30, 0.90, 1.05, 0.10, 1.0, 0.0, false));
        QUALITY_STATS.put("禁忌の", new ItemGenerationResult("", 1.25, 1.05, 1.0, 0.0, 0.95, 0.0, false));
        QUALITY_STATS.put("賢者の", new ItemGenerationResult("", 1.05, 1.15, 1.0, 0.05, 1.10, 0.0, false));
    }

    /**
     * ネームド武器の固定ボーナス（仕様書 6.6）。
     */
    private static final Map<String, ItemGenerationResult> NAMED_WEAPON_STATS = new HashMap<>();

    static {
        NAMED_WEAPON_STATS.put("絶望を刻む者", new ItemGenerationResult("", 1.30, 1.0, 1.0, 0.15, 1.0, 0.05, true));
        NAMED_WEAPON_STATS.put("竜殺しの大剣", new ItemGenerationResult("", 1.35, 1.05, 1.0, 0.10, 1.10, 0.0, true));
        NAMED_WEAPON_STATS.put("虚空を裂く刃", new ItemGenerationResult("", 1.18, 1.0, 1.10, 0.12, 1.0, 0.0, true));
        NAMED_WEAPON_STATS.put("灼熱の剣帝", new ItemGenerationResult("", 1.22, 1.0, 1.05, 0.10, 1.0, 0.0, true));
        NAMED_WEAPON_STATS.put("氷獄の呪い", new ItemGenerationResult("", 1.15, 1.10, 1.0, 0.08, 1.10, 0.0, true));
        NAMED_WEAPON_STATS.put("滅びの咆哮", new ItemGenerationResult("", 1.28, 1.0, 1.0, 0.12, 1.05, 0.0, true));
        NAMED_WEAPON_STATS.put("終焉を告げる者", new ItemGenerationResult("", 1.32, 1.0, 1.0, 0.10, 1.0, 0.04, true));
        NAMED_WEAPON_STATS.put("魔神の嘲笑", new ItemGenerationResult("", 1.20, 0.95, 1.0, 0.18, 1.0, 0.0, true));
        NAMED_WEAPON_STATS.put("深淵を見た者", new ItemGenerationResult("", 1.25, 1.0, 0.95, 0.15, 1.05, 0.0, true));
        NAMED_WEAPON_STATS.put("世界を断つ剣", new ItemGenerationResult("", 1.40, 0.90, 1.0, 0.10, 1.0, 0.0, true));
    }

    private static final String[] ELEMENT_PREFIXES = {
            "烈火の", "氷獄の", "雷轟の", "暴風の", "深淵の", "翠風の", "暗黒の", "聖光の",
            "灼熱の", "氷結の", "雷霆の", "旋風の", "毒霧の", "呪毒の", "邪悪の", "浄化の",
            "月影の", "日輪の", "星芒の", "潮汐の", "震動の", "爆裂の", "殺戮の", "守護の",
            "蒼穹の", "紅葉の", "焔獄の", "黄泉の", "天雷の", "地獄の", "希望の", "黄昏の"
    };

    private static final String[] BEAST_PREFIXES = {
            "獅子の", "鳳凰の", "古龍の", "魔王の", "死神の", "麒麟の", "白虎の", "玄武の",
            "朱雀の", "青龍の", "九尾の", "狼王の", "蛇王の", "鷲王の", "虎王の", "竜王の",
            "精霊王の", "天使の", "堕天使の", "魔神の", "魔女の", "巫女の", "狂戦士の", "吸血鬼の",
            "魔王帝の"
    };

    private static final String[] MATERIAL_PREFIXES = {
            "鋼鉄の", "白金の", "水晶の", "金剛の", "ミスリルの", "オリハルコンの", "黒曜の", "精霊銀の",
            "蒼鉄の", "真鍮の", "翡翠の", "紅玉の", "魔鋼の", "星鉄の", "月光石の", "深海の",
            "竜鋼の", "暗鉄の"
    };

    private static final String[] STATE_PREFIXES = {
            "血濡れの", "輝く", "穢れた", "宿命の", "朽ちた", "新生の", "覚醒した", "封印された",
            "解放された", "闘志の", "幽玄の", "荘厳な", "凶暴な"
    };

    private static final String[] WEAPON_BASES = {
            "剣", "刀", "クレイモア", "ハルバード", "フレイル", "ロングソード", "ショートソード",
            "双剣", "大剣", "曲刀", "サーベル", "レイピア", "バスタードソード", "グレートソード",
            "ナイフ", "ダガー", "短剣", "槍", "ランサー", "トライデント", "ピッチフォーク",
            "斧", "バトルアックス", "ハチェット", "グレートアックス", "メイス", "ハンマー",
            "ウォーハンマー", "モーニングスター", "スタッフ", "ウォンド", "ワンド", "ロッド",
            "ボウ", "ロングボウ", "クロスボウ", "ショートボウ", "クォーターバック", "ブーメラン",
            "ナックル", "クロー", "サイズ", "シャード", "グレイブ", "サイス", "ヌンチャク",
            "グレートフレイル", "デスサイズ"
    };

    private static final String[] ARMOR_BASES = {
            "兜", "フード", "プレート", "ローブ", "グリーブ", "ヘルメット", "チェインメイル",
            "ブーツ", "ガントレット", "キュイラス", "ブレストプレート", "ショルダー", "レッグガード",
            "アーマー", "マント", "クローク", "チュニック", "ベスト", "ジュポン", "スカート",
            "シューズ", "サンダル", "バンド", "ベルト", "サッシュ", "ウィング", "サークレット",
            "ティアラ", "王冠", "マスク", "ヴェール", "ハット", "ボンネット", "ヘルム",
            "ガード", "バックラー", "シールド", "ブレイサー", "コッタ", "スケイルメイル",
            "プレートメイル", "ハーフプレート", "ブリガンダイン", "ロンメル", "ボディアーマー",
            "ファスナー", "グリーブ", "ソレア", "ブーツオブ", "ウィンギャード", "ヘッドピース",
            "オーブ", "タリスマン", "アミュレット", "ペンダント", "リング", "バングル",
            "チャーム", "メダリオン", "ロゼット", "エンブレム", "シジル", "シグネット",
            "ペンドラント", "ブレスレット", "リボン", "チェーン", "トルク", "ネックレス",
            "バッジ", "コイン", "シール", "クリスタル", "ルーン", "グリモワール", "トーム",
            "コード", "スレッド"
    };

    private static final String[] SUFFIXES = {
            "改", "真打", "零式", "・極", "・天命", "・終焉", "・暴威", "・万象", "・無双", "・滅"
    };

    private static final String[] OF_THE_X = {
            "不死鳥", "深淵", "終焉", "混沌", "龍神", "幻獣", "魔王", "死神", "星詠み", "月詠み",
            "黄昏", "黎明", "天穹", "地母神", "海神", "雷神", "風神", "炎神", "氷神", "闇神",
            "光神", "時空", "因果", "輪廻", "永劫", "瞬間", "無限", "虚空", "結界", "破滅",
            "救済", "黎明卿"
    };

    /**
     * 武器生成。70%自動生成 / 30%ネームド。アフィックス補正を含む結果を返す。
     */
    public ItemGenerationResult generateWeapon() {
        if (RANDOM.nextDouble() < 0.70) {
            return generateRandomWeapon();
        }
        return generateNamedWeapon();
    }

    private ItemGenerationResult generateRandomWeapon() {
        String quality = pick(QUALITY_MODIFIERS);
        ItemGenerationResult base = QUALITY_STATS.getOrDefault(quality, ItemGenerationResult.simple(quality, false));
        String baseName = buildWeaponName(quality);
        return new ItemGenerationResult(baseName, base.getAtkMult(), base.getDefMult(), base.getSpdMult(),
                base.getCritBonus(), base.getHpMult(), base.getLifestealBonus(), false);
    }

    private String buildWeaponName(String quality) {
        String base = pick(WEAPON_BASES);
        double roll = RANDOM.nextDouble();
        String result;
        if (roll < 0.15) {
            result = quality + pick(ELEMENT_PREFIXES) + base + " of the " + pick(OF_THE_X);
        } else if (roll < 0.28) {
            result = quality + pick(ELEMENT_PREFIXES) + base;
        } else if (roll < 0.50) {
            result = quality + pick(ELEMENT_PREFIXES) + base + pick(SUFFIXES);
        } else if (roll < 0.70) {
            result = quality + base + " of the " + pick(OF_THE_X);
        } else if (roll < 0.85) {
            result = quality + base + pick(SUFFIXES);
        } else {
            result = quality + base;
        }
        return result;
    }

    public ItemGenerationResult generateNamedWeapon() {
        String[] named = {
                "絶望を刻む者", "竜殺しの大剣", "虚空を裂く刃", "灼熱の剣帝", "氷獄の呪い",
                "滅びの咆哮", "終焉を告げる者", "魔神の嘲笑", "深淵を見た者", "世界を断つ剣"
        };
        String name = pick(named);
        ItemGenerationResult base = NAMED_WEAPON_STATS.getOrDefault(name, ItemGenerationResult.simple(name, true));
        return new ItemGenerationResult(name, base.getAtkMult(), base.getDefMult(), base.getSpdMult(),
                base.getCritBonus(), base.getHpMult(), base.getLifestealBonus(), true);
    }

    /**
     * 防具生成。ネームド固定は無し、品質補正のみ。
     */
    public ItemGenerationResult generateArmor() {
        String quality = pick(QUALITY_MODIFIERS);
        ItemGenerationResult base = QUALITY_STATS.getOrDefault(quality, ItemGenerationResult.simple(quality, false));
        String name = buildArmorName(quality);
        return new ItemGenerationResult(name, base.getAtkMult(), base.getDefMult(), base.getSpdMult(),
                base.getCritBonus(), base.getHpMult(), base.getLifestealBonus(), false);
    }

    private String buildArmorName(String quality) {
        String base = pick(ARMOR_BASES);
        double roll = RANDOM.nextDouble();
        String result;
        if (roll < 0.15) {
            result = quality + pick(MATERIAL_PREFIXES) + base + " of the " + pick(OF_THE_X);
        } else if (roll < 0.30) {
            result = quality + pick(MATERIAL_PREFIXES) + base;
        } else if (roll < 0.50) {
            result = quality + base + " of the " + pick(OF_THE_X);
        } else if (roll < 0.70) {
            result = quality + base + pick(SUFFIXES);
        } else {
            result = quality + base;
        }
        return result;
    }

    /**
     * ネームド防具生成（ボーナス付き）。
     */
    public ItemGenerationResult generateNamedArmor() {
        String[] named = {
                "不屈の防壁", "魔王の鎧", "聖者の聖衣", "影渡りの衣", "暁の守護",
                "深淵の護り手", "神々の加護", "絶対防壁", "混沌の守護者", "天空の鎧"
        };
        String name = pick(named);
        ItemGenerationResult base = NAMED_ARMOR_STATS.getOrDefault(name, ItemGenerationResult.simple(name, true));
        return new ItemGenerationResult(name, base.getAtkMult(), base.getDefMult(), base.getSpdMult(),
                base.getCritBonus(), base.getHpMult(), base.getLifestealBonus(), true);
    }

    private static final Map<String, ItemGenerationResult> NAMED_ARMOR_STATS = new HashMap<>();

    static {
        NAMED_ARMOR_STATS.put("不屈の防壁", new ItemGenerationResult("", 0.95, 1.30, 1.0, 0.0, 1.20, 0.0, true));
        NAMED_ARMOR_STATS.put("魔王の鎧", new ItemGenerationResult("", 1.10, 1.20, 1.0, 0.05, 1.15, 0.0, true));
        NAMED_ARMOR_STATS.put("聖者の聖衣", new ItemGenerationResult("", 1.0, 1.15, 1.05, 0.05, 1.20, 0.02, true));
        NAMED_ARMOR_STATS.put("影渡りの衣", new ItemGenerationResult("", 1.0, 1.05, 1.20, 0.10, 1.10, 0.0, true));
        NAMED_ARMOR_STATS.put("暁の守護", new ItemGenerationResult("", 0.95, 1.25, 1.0, 0.0, 1.25, 0.0, true));
        NAMED_ARMOR_STATS.put("深淵の護り手", new ItemGenerationResult("", 1.05, 1.20, 1.0, 0.05, 1.15, 0.0, true));
        NAMED_ARMOR_STATS.put("神々の加護", new ItemGenerationResult("", 1.0, 1.15, 1.0, 0.10, 1.20, 0.0, true));
        NAMED_ARMOR_STATS.put("絶対防壁", new ItemGenerationResult("", 0.90, 1.35, 0.95, 0.0, 1.20, 0.0, true));
        NAMED_ARMOR_STATS.put("混沌の守護者", new ItemGenerationResult("", 1.10, 1.15, 1.0, 0.08, 1.10, 0.0, true));
        NAMED_ARMOR_STATS.put("天空の鎧", new ItemGenerationResult("", 1.05, 1.20, 1.05, 0.05, 1.15, 0.0, true));
    }

    public String generateWeaponName() {
        return generateWeapon().getName();
    }

    public String generateArmorName() {
        return generateArmor().getName();
    }

    public String generateNamedWeaponName() {
        return generateNamedWeapon().getName();
    }

    private String pick(String[] array) {
        return array[RANDOM.nextInt(array.length)];
    }
}
