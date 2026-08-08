package com.dungeoncrawler.listener;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.game.LootManager;
import com.dungeoncrawler.mob.CustomMobManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * ダンジョン内でスポーンしたMOB/BOSS/エリートの死亡時ドロップ処理。
 */
public class MobDropListener implements Listener {

    private final DepthCrawlerPlugin plugin;

    public MobDropListener(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.getWorld().getName().startsWith("dungeon_"))) {
            return;
        }
        PersistentDataContainer c = entity.getPersistentDataContainer();
        Integer tier = c.get(CustomMobManager.MOB_KEY, PersistentDataType.INTEGER);
        if (tier == null) {
            return; // バニラモブ等はスルー
        }
        Integer floor = c.get(CustomMobManager.SPAWN_FLOOR, PersistentDataType.INTEGER);
        int fl = floor == null ? 1 : floor;

        LootManager loot = plugin.getLootManager();
        List<ItemStack> drops;
        if (tier == 2) {
            drops = loot.rollBossDrop(fl);
        } else if (tier == 1) {
            drops = loot.rollEliteDrop(fl);
        } else {
            drops = loot.rollMobDrop(fl);
        }

        Location loc = entity.getLocation();
        for (ItemStack item : drops) {
            if (item != null) {
                entity.getWorld().dropItemNaturally(loc, item);
            }
        }
    }
}
