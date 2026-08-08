package com.dungeoncrawler.item;

/**
 * 武器/防具の生成結果。表示名と、名前のアフィックス（品質/ネームド）によるステータス補正を保持する。
 * 倍率系は1.0が基準、加算系は0が基準。
 */
public class ItemGenerationResult {

    private final String name;
    private final double atkMult;
    private final double defMult;
    private final double spdMult;
    private final double critBonus;
    private final double hpMult;
    private final double lifestealBonus;
    private final boolean named;

    public ItemGenerationResult(String name, double atkMult, double defMult, double spdMult,
                                double critBonus, double hpMult, double lifestealBonus, boolean named) {
        this.name = name;
        this.atkMult = atkMult;
        this.defMult = defMult;
        this.spdMult = spdMult;
        this.critBonus = critBonus;
        this.hpMult = hpMult;
        this.lifestealBonus = lifestealBonus;
        this.named = named;
    }

    public static ItemGenerationResult simple(String name, boolean named) {
        return new ItemGenerationResult(name, 1.0, 1.0, 1.0, 0, 1.0, 0, named);
    }

    public String getName() {
        return name;
    }

    public double getAtkMult() {
        return atkMult;
    }

    public double getDefMult() {
        return defMult;
    }

    public double getSpdMult() {
        return spdMult;
    }

    public double getCritBonus() {
        return critBonus;
    }

    public double getHpMult() {
        return hpMult;
    }

    public double getLifestealBonus() {
        return lifestealBonus;
    }

    public boolean isNamed() {
        return named;
    }
}
