package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.mob.CustomMobType;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DifficultyManager {

    private final DepthCrawlerPlugin plugin;
    private final Random random = new Random();

    public DifficultyManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    public int spawnCount(int floor, FloorModifier modifier) {
        int count = 30 + floor * 3;
        if (modifier == FloorModifier.SWARM) {
            count = (int) (count * 1.5);
        }
        return count;
    }

    public int phase(int floor) {
        if (floor >= 51) return 6;
        if (floor >= 41) return 5;
        if (floor >= 31) return 4;
        if (floor >= 21) return 3;
        if (floor >= 11) return 2;
        return 1;
    }

    public double eliteChance(int floor) {
        return 0.10 + (phase(floor) - 1) * 0.03;
    }

    public List<EntityType> mobPoolForFloor(int floor) {
        int phase = phase(floor);
        List<EntityType> pool = new ArrayList<>();
        for (CustomMobType type : CustomMobType.values()) {
            if (type.getMinPhase() <= phase && type.isNormalMob()) {
                pool.add(type.getEntityType());
            }
        }
        if (pool.isEmpty()) {
            pool.add(EntityType.ZOMBIE);
        }
        return pool;
    }

    public EntityType randomMob(int floor) {
        List<EntityType> pool = mobPoolForFloor(floor);
        return pool.get(random.nextInt(pool.size()));
    }

    public EntityType randomBoss() {
        EntityType[] bosses = {
                EntityType.IRON_GOLEM, EntityType.ZOMBIE, EntityType.ENDERMAN,
                EntityType.RAVAGER, EntityType.WITHER, EntityType.WARDEN,
                EntityType.EVOKER, EntityType.PIGLIN_BRUTE, EntityType.ELDER_GUARDIAN,
                EntityType.ENDER_DRAGON
        };
        return bosses[random.nextInt(bosses.length)];
    }
}
