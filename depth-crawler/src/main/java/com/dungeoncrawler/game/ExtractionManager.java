package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.dungeon.FloorGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExtractionManager {

    private final DepthCrawlerPlugin plugin;
    private final Map<UUID, Long> standingSince = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> warned = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> rushTriggered = new ConcurrentHashMap<>();

    public ExtractionManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 出口ブロック上に立っているプレイヤーの抽出進行を処理する。
     */
    public void tick(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            clear(player);
            return;
        }
        Location loc = player.getLocation();
        Material under = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        Material standing = player.getLocation().getBlock().getType();

        boolean emerald = standing == Material.EMERALD_BLOCK || under == Material.EMERALD_BLOCK;
        boolean gold = standing == Material.GOLD_BLOCK || under == Material.GOLD_BLOCK;

        if (!emerald && !gold) {
            clear(player);
            return;
        }

        long now = System.currentTimeMillis();
        long start = standingSince.getOrDefault(player.getUniqueId(), now);
        if (standingSince.get(player.getUniqueId()) == null) {
            standingSince.put(player.getUniqueId(), now);
            warned.put(player.getUniqueId(), false);
            player.sendMessage("§e出口検知... 3秒間その場に留まってください。");
        }
        double elapsed = (now - start) / 1000.0;
        player.spawnParticle(Particle.PORTAL, loc, 20, 0.5, 0.5, 0.5, 0.1);

        if (elapsed >= 1.5 && !warned.getOrDefault(player.getUniqueId(), false)) {
            warned.put(player.getUniqueId(), true);
            player.sendMessage("§c1.5秒経過... まもなく発動します！");
        }
        // ゴールド（帰還）検知時、撤退ラッシュを一度だけ発生
        if (gold && !rushTriggered.getOrDefault(player.getUniqueId(), false)) {
            rushTriggered.put(player.getUniqueId(), true);
            plugin.getCustomMobManager().spawnExtractionRush(player, session.getFloor());
            player.sendMessage("§c§l撤退ラッシュ! 魔物が押し寄せてくる!");
        }
        if (elapsed >= plugin.getDungeonConfig().getExitStandSeconds()) {
            if (emerald) {
                plugin.getGameManager().advanceFloor(player);
            } else {
                plugin.getGameManager().extract(player);
            }
            clear(player);
        }
    }

    public void clear(Player player) {
        standingSince.remove(player.getUniqueId());
        warned.remove(player.getUniqueId());
        rushTriggered.remove(player.getUniqueId());
    }
}
