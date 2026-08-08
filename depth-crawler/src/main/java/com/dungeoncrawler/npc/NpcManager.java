package com.dungeoncrawler.npc;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.item.ItemRegistry;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NpcManager {

    private static NamespacedKey npcTypeKey;

    private final DepthCrawlerPlugin plugin;

    public NpcManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        npcTypeKey = new NamespacedKey(plugin, "npc_type");
    }

    public static NamespacedKey getNpcTypeKey() {
        return npcTypeKey;
    }

    /**
     * NPCをスポーンさせる。
     */
    public void spawnNpc(NpcType type, Location location) {
        Villager villager = (Villager) location.getWorld().spawn(location, Villager.class);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setCollidable(false);
        villager.setGravity(true);
        villager.setCustomName(type.getDisplay());
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.valueOf(type.getProfession()));
        villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false));
        villager.getPersistentDataContainer().set(npcTypeKey, PersistentDataType.STRING, type.name());
    }

    /**
     * NPCの種別を返す。
     */
    public NpcType getType(Entity entity) {
        if (entity == null || !(entity instanceof Villager villager)) {
            return null;
        }
        String value = villager.getPersistentDataContainer().get(npcTypeKey, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        return NpcType.fromString(value);
    }

    /**
     * NPCを撤去する（ブラックマーケット用にタグを除去）。
     */
    public void removeNpc(Entity entity) {
        if (entity instanceof Villager villager) {
            villager.getPersistentDataContainer().remove(npcTypeKey);
        }
    }
}
