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
        int count = difficulty.spawnCount(floor, modifier);
        int size = generator.getFloorSize();
        int originZ = generator.floorStartZ(floor);
        int groundY = generator.getBaseY() + 1;

        for (int i = 0; i < count; i++) {
            // 空気ブロックを探す（最大20回試行）
            for (int attempt = 0; attempt < 20; attempt++) {
                int x = 1 + random.nextInt(size - 2);
                int z = 1 + random.nextInt(size - 2);
                if (world.getBlockAt(x, groundY, originZ + z).getType().isAir()
                        && world.getBlockAt(x, groundY + 1, originZ + z).getType().isAir()) {
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
    public void spawnBoss(World world, Location location) {
        EntityType boss = plugin.getDifficultyManager().randomBoss();
        spawnEntity(world, location, boss, true, 50, null);
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
        double defMult = modifier != null ? modifier.getDefMult() : 1.0;
        double spdMult = modifier != null ? modifier.getSpdMult() : 1.0;

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
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHp);
        entity.setHealth(baseHp);
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(5.0 * atkMult);
        if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25 * spdMult);
        }
        // 種別マーキング（ドロップ判定用）
        entity.getPersistentDataContainer().set(MOB_KEY, PersistentDataType.INTEGER,
                boss ? 2 : (elite ? 1 : 0));
        entity.getPersistentDataContainer().set(SPAWN_FLOOR, PersistentDataType.INTEGER, Math.max(1, floor));
        if (modifier == FloorModifier.DARKNESS) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
        if (modifier == FloorModifier.REGENERATION) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        }
        if (modifier == FloorModifier.HASTE) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
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
        if (session == null) {
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
            if (center.getWorld().getBlockAt(x, groundY, z).getType().isAir()) {
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
                if (center.getWorld().getBlockAt(x, center.getBlockY() - 1, z).getType().isAir()
                        && center.getWorld().getBlockAt(x, center.getBlockY(), z).getType().isAir()) {
                    Location loc = new Location(center.getWorld(), x + 0.5, center.getBlockY(), z + 0.5);
                    spawnMob(center.getWorld(), loc, Math.max(1, floor), modifier);
                    break;
                }
            }
        }
    }
}
