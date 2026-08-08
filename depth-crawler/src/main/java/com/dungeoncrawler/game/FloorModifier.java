package com.dungeoncrawler.game;

public enum FloorModifier {

    DARKNESS("§8暗闇", 1.0, 1.0, 1.0, 1.0),
    BERSERK("§c狂乱", 1.3, 1.0, 1.0, 1.2),
    FORTIFIED("§7要塞", 1.0, 1.25, 1.0, 1.0),
    HASTE("§b加速", 1.0, 1.0, 1.2, 1.0),
    PLAGUE("§2疫病", 1.0, 1.0, 1.0, 1.3),
    TREASURE("§6宝物庫", 1.0, 1.0, 1.0, 1.0),
    SWARM("§e大群", 1.0, 1.0, 1.0, 1.0),
    WEAKNESS("§8脆弱", 1.0, 1.0, 1.0, 1.25),
    REGENERATION("§d再生", 1.0, 1.0, 1.0, 1.0);

    private final String display;
    private final double atkMult;
    private final double defMult;
    private final double spdMult;
    private final double dropMult;

    FloorModifier(String display, double atkMult, double defMult, double spdMult, double dropMult) {
        this.display = display;
        this.atkMult = atkMult;
        this.defMult = defMult;
        this.spdMult = spdMult;
        this.dropMult = dropMult;
    }

    public String getDisplay() {
        return display;
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

    public double getDropMult() {
        return dropMult;
    }

    public static FloorModifier random() {
        FloorModifier[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
}
