package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonManager;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.dungeon.FloorGenerator;
import com.dungeoncrawler.player.PlayerData;
import com.dungeoncrawler.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameManager {

    private final DepthCrawlerPlugin plugin;
    private final Map<UUID, Integer> runTaskIds = new HashMap<>();
    private final Map<UUID, Location> hubLocations = new HashMap<>();
    private final Set<UUID> inDungeon = new HashSet<>();

    public GameManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * ダンジョン入室（リーダー）。パーティメンバーも一緒にTPする。
     */
    public void enter(Player player) {
        DungeonManager manager = plugin.getDungeonManager();
        if (manager.getSession(player.getUniqueId()) != null) {
            player.sendMessage("§cすでにダンジョンに参加しています。");
            return;
        }

        DungeonSession session = manager.createSession(player);
        FloorGenerator generator = new FloorGenerator(plugin);
        boolean treasure = session.getModifier() == FloorModifier.TREASURE;
        generator.generate(session.getWorld(), 0, false, false, treasure);

        player.setGameMode(GameMode.ADVENTURE);
        hubLocations.put(player.getUniqueId(), player.getLocation());
        inDungeon.add(player.getUniqueId());
        player.teleport(manager.getSpawn(session));
        player.sendMessage("§6§lDUNGEON 開始! 出口を目指して進め!");
        plugin.getDungeonManager().setOnline(player.getUniqueId(), true);
        updateScoreboard(player);
        startRunTasks(session, player);

        // 初期湧き（floor 0）
        plugin.getCustomMobManager().spawnInitialMobs(session);

        // パーティメンバーを一緒にTP
        Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party != null) {
            for (Player member : plugin.getPartyManager().onlineMembers(party)) {
                if (member.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }
                joinSessionTeleport(session, member);
            }
        }
    }

    private void joinSessionTeleport(DungeonSession session, Player member) {
        member.setGameMode(GameMode.ADVENTURE);
        hubLocations.put(member.getUniqueId(), member.getLocation());
        inDungeon.add(member.getUniqueId());
        plugin.getDungeonManager().joinSession(session, member);
        member.sendMessage("§6パーティリーダーがダンジョンに入室しました。");
        updateScoreboard(member);
    }

    public void joinRun(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            enter(player);
            return;
        }
        player.setGameMode(GameMode.ADVENTURE);
        hubLocations.putIfAbsent(player.getUniqueId(), player.getLocation());
        inDungeon.add(player.getUniqueId());
        player.teleport(plugin.getDungeonManager().getSpawn(session));
        updateScoreboard(player);
    }

    /**
     * フロア進行（エメラルドブロック）。
     */
    public void advanceFloor(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        int next = session.getFloor() + 1;
        session.setFloor(next);
        session.setModifier(FloorModifier.random());
        session.setState(GameState.EXPLORING);

        FloorGenerator generator = new FloorGenerator(plugin);
        boolean rest = session.isRestFloor();
        boolean boss = session.isBossFloor();
        boolean treasure = session.getModifier() == FloorModifier.TREASURE;
        generator.generate(session.getWorld(), next, rest, boss, treasure);

        for (UUID uuid : session.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.teleport(generator.getSpawnLocation(session.getWorld(), next));
                p.sendMessage("§e=== " + next + "F ===  修飾子: " + session.getModifier().getDisplay());
                if (rest) {
                    p.sendMessage("§b休息フロア! 出口の石ボタンで強化を選択できます。");
                }
                if (boss) {
                    p.sendMessage("§c§lBOSSフロア! 注意せよ!");
                }
                applyModifierEffects(p, session.getModifier());
                updateScoreboard(p);
            }
        }
        if (boss) {
            plugin.getCustomMobManager().spawnBoss(session.getWorld(), generator.getSpawnLocation(session.getWorld(), next));
        }
        plugin.getCustomMobManager().spawnInitialMobs(session);
    }

    /**
     * 帰還（ゴールドブロック）。セッションから退出し拠点へTP。
     */
    public void extract(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.incrementTotalRuns();
        data.incrementSuccessfulExtractions();
        data.setHighestFloor(session.getFloor());

        int reward = 100 + session.getFloor() * 50 + session.getStreak() * 10;
        data.addBalance(reward);
        player.sendMessage("§a§l生還! 報酬 " + reward + "G を獲得!");
        player.sendMessage("§e報酬が入りました。装備は持ち帰りです。");

        leaveSession(player, session);
    }

    /**
     * 死亡処理（装備全ロスト・保険退避）。
     */
    public void handlePlayerDeath(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        plugin.getInsuranceManager().evacuateOnDeath(player);
        plugin.getDungeonManager().setOnline(player.getUniqueId(), false);

        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        data.incrementTotalRuns();
        data.setHighestFloor(session.getFloor());
        data.setStreak(0);

        player.sendMessage("§c§l死亡... 未保険の装備は全ロストしました。");

        // リスポーン地点を拠点に設定（PlayerRespawnEvent で拠点へTP）
        Location hub = hubLocations.get(player.getUniqueId());
        if (hub != null) {
            player.setBedSpawnLocation(hub, true);
        } else {
            player.setBedSpawnLocation(getDefaultHub(), true);
        }

        leaveSession(player, session, true);
    }

    /**
     * 死亡リスポーン時に拠点へTPし、通常状態に戻す。
     */
    public void handleRespawn(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        Location hub = hubLocations.remove(player.getUniqueId());
        player.teleport(hub != null ? hub : getDefaultHub());
        plugin.getScoreboardManager().clear(player);
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session != null) {
            session.removeMember(player.getUniqueId());
            inDungeon.remove(player.getUniqueId());
            if (session.getMembers().isEmpty()) {
                endRun(session);
            }
        }
    }

    /**
     * セッションから退出し、全員いなくなればセッションを破棄。
     */
    private void leaveSession(Player player, DungeonSession session) {
        leaveSession(player, session, false);
    }

    private void leaveSession(Player player, DungeonSession session, boolean death) {
        if (death) {
            // 死亡・切断時もセッションから除去（リスポーン/再接続時は再度処理しても無害）
            session.removeMember(player.getUniqueId());
            inDungeon.remove(player.getUniqueId());
            if (session.getMembers().isEmpty()) {
                endRun(session);
            }
            return;
        }
        session.removeMember(player.getUniqueId());
        inDungeon.remove(player.getUniqueId());
        Location hub = hubLocations.remove(player.getUniqueId());
        player.teleport(hub != null ? hub : getDefaultHub());
        player.setGameMode(GameMode.SURVIVAL);
        plugin.getScoreboardManager().clear(player);

        if (session.getMembers().isEmpty()) {
            endRun(session);
        } else {
            // 残りメンバーに通知
            for (UUID uuid : session.getMembers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendMessage("§e" + player.getName() + " が生還しました。");
                }
            }
        }
    }

    public void endRun(DungeonSession session) {
        Integer taskId = runTaskIds.remove(session.getSessionId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        plugin.getDungeonManager().removeSession(session);
    }

    public void returnToHub(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session != null) {
            leaveSession(player, session);
        }
    }

    public Location getDefaultHub() {
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    public boolean isInDungeon(Player player) {
        return inDungeon.contains(player.getUniqueId());
    }

    public void updateScoreboard(Player player) {
        plugin.getScoreboardManager().update(player);
    }

    public void shutdown() {
        for (int id : runTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(id);
        }
        runTaskIds.clear();
    }

    private void startRunTasks(DungeonSession session, Player leader) {
        int[] tickCounter = {0};
        int tickId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tickCounter[0]++;
            for (UUID uuid : session.getMembers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    plugin.getScoreboardManager().update(p);
                    plugin.getExtractionManager().tick(p);
                    // PLAGUE修飾子: 毎2秒1ダメージ
                    if (session.getModifier() == FloorModifier.PLAGUE && tickCounter[0] % 2 == 0) {
                        if (p.getHealth() > 1.0) {
                            p.damage(1.0);
                        }
                    }
                }
            }
        }, 20L, 20L).getTaskId();
        runTaskIds.put(session.getSessionId(), tickId);
    }

    private void applyModifierEffects(Player p, FloorModifier modifier) {
        switch (modifier) {
            case DARKNESS -> p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 60, 0));
            case HASTE -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 0));
            case REGENERATION -> p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60, 0));
            case WEAKNESS -> p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 60, 0));
            default -> {
            }
        }
    }
}
