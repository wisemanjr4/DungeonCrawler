package com.dungeoncrawler.mob;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.dungeon.FloorGenerator;
import com.dungeoncrawler.game.DifficultyManager;
import com.dungeoncrawler.game.FloorModifier;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CustomMobManager {

    public static final NamespacedKey MOB_KEY = new NamespacedKey("depthcrawler", "mob_tier");
    public static final NamespacedKey SPAWN_FLOOR = new NamespacedKey("depthcrawler", "spawn_floor");

    private final DepthCrawlerPlugin plugin;
    private final Random random = new Random();

    public CustomMobManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * フロア開始時の初期湧き。空気ブロックの場所にスポーンする。
     */
    public void spawnInitialMobs(DungeonSession session) {
        World world = session.getWorld();
        FloorGenerator generator = new FloorGenerator(plugin);
        DifficultyManager difficulty = plugin.getDifficultyManager();
        int floor = session.getFloor();
        FloorModifier modifier = session.getModifier();
        if (session.isRestFloor()) {
            return; // 休息フロアは敵を湧かせない
        }
        int count = difficulty.spawnCount(floor, modifier);
        int size = generator.getFloorSize();
        int originZ = generator.floorStartZ(floor);
        int groundY = generator.getBaseY() + 1;

        for (int i = 0; i < count; i++) {
            // 空気ブロックを探す（最大20回試行）
            for (int attempt = 0; attempt < 20; attempt++) {
                int x = 1 + random.nextInt(size - 2);
                int z = 1 + random.nextInt(size - 2);
                // 開始部屋(0,0)はスポーン直後に囲まれないよう安全地帯にする
                boolean startRoom = x <= generator.getCell() && z <= generator.getCell();
                if (!startRoom && isSpawnable(world, x, groundY, originZ + z)) {
                    Location loc = new Location(world, x + 0.5, groundY + 1, originZ + z + 0.5);
                    spawnMob(world, loc, floor, modifier);
                    break;
                }
            }
        }
    }

    /**
     * BOSSスポーン。
     */
    public void spawnBoss(World world, Location location, int floor) {
        EntityType boss = plugin.getDifficultyManager().randomBoss();
        // floor はドロップのティア（MobDropListener が SPAWN_FLOOR から読む）に使われる
        spawnEntity(world, location, boss, true, Math.max(1, floor), null);
    }

    public void spawnMob(World world, Location loc, int floor, FloorModifier modifier) {
        EntityType type = plugin.getDifficultyManager().randomMob(floor);
        boolean elite = random.nextDouble() < plugin.getDifficultyManager().eliteChance(floor);
        spawnEntity(world, loc, type, false, floor, modifier, elite);
    }

    private void spawnEntity(World world, Location loc, EntityType type, boolean boss, int floor, FloorModifier modifier) {
        spawnEntity(world, loc, type, boss, floor, modifier, false);
    }

    private void spawnEntity(World world, Location loc, EntityType type, boolean boss, int floor, FloorModifier modifier, boolean elite) {
        LivingEntity entity = (LivingEntity) world.spawnEntity(loc, type);
        double atkMult = modifier != null ? modifier.getAtkMult() : 1.0;

        double baseHp = 20.0 + (floor > 0 ? floor : 1) * 4.0;
        if (boss) {
            baseHp = 200.0;
            entity.setCustomName("§c§lBOSS");
            entity.setCustomNameVisible(true);
        } else if (elite) {
            baseHp *= 2.5;
            atkMult *= 1.4;
            entity.setCustomName("§d§lエリート");
            entity.setCustomNameVisible(true);
        }
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHp);
        }
        entity.setHealth(Math.min(baseHp, entity.getMaxHealth()));
        // 攻撃力属性を持たない種別（スケルトン等）もあるためnullチェック
        if (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(5.0 * atkMult);
        }
        // 種別マーキング（ドロップ判定用）
        entity.getPersistentDataContainer().set(MOB_KEY, PersistentDataType.INTEGER,
                boss ? 2 : (elite ? 1 : 0));
        entity.getPersistentDataContainer().set(SPAWN_FLOOR, PersistentDataType.INTEGER, Math.max(1, floor));
        if (modifier == FloorModifier.REGENERATION) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        }
    }

    /**
     * 動的湧き: 2秒ごと1/15の確率でプレイヤー周囲21×21に1体。
     */
    /**
     * 動的湧き: 2秒ごと1/15の確率でプレイヤー周囲21×21に1体。
     */
    public void dynamicSpawn(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null || session.isRestFloor()) {
            return;
        }
        if (random.nextInt(15) != 0) {
            return;
        }
        FloorGenerator generator = new FloorGenerator(plugin);
        int groundY = generator.getBaseY() + 1;
        Location center = player.getLocation();
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = center.getBlockX() + random.nextInt(21) - 10;
            int z = center.getBlockZ() + random.nextInt(21) - 10;
            boolean tooClose = Math.abs(x - center.getBlockX()) < 4 && Math.abs(z - center.getBlockZ()) < 4;
            if (!tooClose && isSpawnable(center.getWorld(), x, groundY, z)) {
                Location loc = new Location(center.getWorld(), x + 0.5, groundY + 1, z + 0.5);
                spawnMob(center.getWorld(), loc, session.getFloor(), session.getModifier());
                return;
            }
        }
    }

    /**
     * 撤退ラッシュ: 帰還中にプレイヤー周囲へ魔物を押し寄せさせる。
     */
    public void spawnExtractionRush(Player player, int floor) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        FloorModifier modifier = session != null ? session.getModifier() : null;
        Location center = player.getLocation();
        for (int i = 0; i < 6; i++) {
            for (int attempt = 0; attempt < 10; attempt++) {
                int x = center.getBlockX() + random.nextInt(15) - 7;
                int z = center.getBlockZ() + random.nextInt(15) - 7;
                if (isSpawnable(center.getWorld(), x, center.getBlockY() - 1, z)) {
                    Location loc = new Location(center.getWorld(), x + 0.5, center.getBlockY(), z + 0.5);
                    spawnMob(center.getWorld(), loc, Math.max(1, floor), modifier);
                    break;
                }
            }
        }
    }

    /** 床が固体で、その上2マスが空気（湧いても壁や穴に埋まらない）。 */
    private static boolean isSpawnable(World world, int x, int floorY, int z) {
        return world.getBlockAt(x, floorY, z).getType().isSolid()
                && world.getBlockAt(x, floorY + 1, z).getType().isAir()
                && world.getBlockAt(x, floorY + 2, z).getType().isAir();
    }
}
