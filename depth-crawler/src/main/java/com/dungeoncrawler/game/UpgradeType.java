package com.dungeoncrawler.game;

public enum UpgradeType {

    ATTACK("攻撃力UP", "ATK+25%", false, 3),
    DEFENSE("防御力UP", "被ダメ-15%", false, 3),
    SPEED("速度UP", "移動+20%", false, 3),
    CRIT("会心UP", "CRIT率+10%", false, 3),
    LIFE_STEAL("ライフスティール", "攻撃時5%吸血", false, 3),
    REGEN("自動回復", "HP+2/sec", false, 3),
    DODGE("回避UP", "回避率+6%", false, 3),
    COUNTER("反撃", "被ダメ8%反射", false, 3),
    DOUBLE_ATTACK("二段攻撃", "20%で追撃", false, 3),
    AOE("範囲攻撃", "周囲30%ダメ", false, 3),
    CHEST_BOOST("宝箱強化", "報酬+50%", false, 3),
    ELEMENTAL("属性強化", "属性ダメ+30%", false, 3),
    CHAIN_LIGHTNING("連鎖電撃", "近くの敵2体に連鎖ダメ30%", true, 1),
    EXPLOSIVE_HIT("爆裂の一撃", "10%で爆発+範囲ダメ50%", true, 1),
    BERSERKER("狂戦士", "キル毎ATK+5%（5重/10秒）", true, 1),
    ADRENALINE("アドレナリン", "HP30%以下でATK+35%/SPD+20%", true, 1),
    GLASS_CANNON("ガラスの大砲", "ATK+40% / DEF-20%", true, 1),
    BLOOD_AURA("吸血のオーラ", "5ブロック以内の敵からHP吸収", true, 1),
    LUCK("幸運の発見", "ドロップ品質+25%", true, 1);

    private final String name;
    private final String description;
    private final boolean rare;
    private final int maxLevel;

    UpgradeType(String name, String description, boolean rare, int maxLevel) {
        this.name = name;
        this.description = description;
        this.rare = rare;
        this.maxLevel = maxLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRare() {
        return rare;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
