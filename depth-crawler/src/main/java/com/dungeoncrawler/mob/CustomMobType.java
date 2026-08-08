package com.dungeoncrawler.mob;

import org.bukkit.entity.EntityType;

public enum CustomMobType {

    ZOMBIE_NORMAL("ゾンビ", EntityType.ZOMBIE, MobClass.NORMAL, 1),
    SKELETON_NORMAL("スケルトン", EntityType.SKELETON, MobClass.NORMAL, 1),
    SPIDER_NORMAL("スパイダー", EntityType.SPIDER, MobClass.NORMAL, 1),
    SLIME_NORMAL("スライム", EntityType.SLIME, MobClass.NORMAL, 1),
    PIGLIN_NORMAL("ピグリン", EntityType.PIGLIN, MobClass.NORMAL, 1),
    HOGLIN_NORMAL("ホグリン", EntityType.HOGLIN, MobClass.NORMAL, 1),
    WOLF_NORMAL("狼", EntityType.WOLF, MobClass.NORMAL, 1),
    POLAR_BEAR_NORMAL("シロクマ", EntityType.POLAR_BEAR, MobClass.NORMAL, 1),
    IRON_GOLEM_NORMAL("アイアンゴーレム", EntityType.IRON_GOLEM, MobClass.NORMAL, 2),
    WARDEN_NORMAL("ウォーデン", EntityType.WARDEN, MobClass.NORMAL, 3),

    SKELETON_RANGED("スケルトン弓兵", EntityType.SKELETON, MobClass.RANGED, 1),
    STRAY_RANGED("ストレイ", EntityType.STRAY, MobClass.RANGED, 1),
    WITHER_SKELETON_RANGED("ウィザースケルトン", EntityType.WITHER_SKELETON, MobClass.RANGED, 1),
    PILLAGER_RANGED("ピリジャー", EntityType.PILLAGER, MobClass.RANGED, 1),
    BLAZE_RANGED("ブレイズ", EntityType.BLAZE, MobClass.RANGED, 2),
    SHULKER_RANGED("シュルカー", EntityType.SHULKER, MobClass.RANGED, 2),
    GUARDIAN_RANGED("ガーディアン", EntityType.GUARDIAN, MobClass.RANGED, 2),

    CAVE_SPIDER_FAST("洞窟蜘蛛", EntityType.CAVE_SPIDER, MobClass.FAST, 1),
    VEX_FAST("ヴェックス", EntityType.VEX, MobClass.FAST, 2),
    PHANTOM_FAST("ファントム", EntityType.PHANTOM, MobClass.FAST, 2),
    ENDERMITE_FAST("エンダーマイト", EntityType.ENDERMITE, MobClass.FAST, 1),
    SILVERFISH_FAST("シルバーフィッシュ", EntityType.SILVERFISH, MobClass.FAST, 1),

    CREEPER_EXPLOSIVE("クリーパー", EntityType.CREEPER, MobClass.EXPLOSIVE, 2),
    GHAST_EXPLOSIVE("ガスト", EntityType.GHAST, MobClass.EXPLOSIVE, 2),

    RAVAGER_HEAVY("ラヴェジャー", EntityType.RAVAGER, MobClass.HEAVY, 2),
    ENDERMAN_HEAVY("エンダーマン", EntityType.ENDERMAN, MobClass.HEAVY, 2),
    ZOMBIE_HEAVY("ヘヴィゾンビ", EntityType.ZOMBIE, MobClass.HEAVY, 2),

    WITCH_CASTER("ウィッチ", EntityType.WITCH, MobClass.CASTER, 4),
    EVOKER_CASTER("エヴォーカー", EntityType.EVOKER, MobClass.CASTER, 4),
    ILLUSIONER_CASTER("イリュージョナー", EntityType.ILLUSIONER, MobClass.CASTER, 4),

    BEE_FLYING("ハチ", EntityType.BEE, MobClass.FLYING, 4);

    private final String displayName;
    private final EntityType entityType;
    private final MobClass mobClass;
    private final int minPhase;

    CustomMobType(String displayName, EntityType entityType, MobClass mobClass, int minPhase) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.mobClass = mobClass;
        this.minPhase = minPhase;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public MobClass getMobClass() {
        return mobClass;
    }

    public int getMinPhase() {
        return minPhase;
    }

    public boolean isNormalMob() {
        return true;
    }

    public enum MobClass {
        NORMAL, RANGED, FAST, EXPLOSIVE, HEAVY, CASTER, FLYING, ELITE, BOSS
    }
}
