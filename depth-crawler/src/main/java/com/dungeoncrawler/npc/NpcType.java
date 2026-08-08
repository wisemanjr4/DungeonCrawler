package com.dungeoncrawler.npc;

public enum NpcType {

    SHOP("§6ショップ", "PRIEST"),
    SAFEBOX("§8セーフティボックス", "CARTOGRAPHER"),
    DUNGEON_GUIDE("§cダンジョン案内人", "WEAPONSMITH"),
    BLACKSMITH("§d鍛冶屋", "TOOLSMITH"),
    RELIEF("§b救助班", "NITWIT"),
    BLACKMARKET("§5ブラックマーケット", "NITWIT");

    private final String display;
    private final String profession;

    NpcType(String display, String profession) {
        this.display = display;
        this.profession = profession;
    }

    public String getDisplay() {
        return display;
    }

    public String getProfession() {
        return profession;
    }

    public static NpcType fromString(String value) {
        for (NpcType type : values()) {
            if (type.name().equalsIgnoreCase(value) || type.name().replace("_", "").equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
