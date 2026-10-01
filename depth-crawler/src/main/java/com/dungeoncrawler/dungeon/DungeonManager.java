package com.dungeoncrawler.dungeon;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.game.GameState;
import com.dungeoncrawler.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonManager {

    private final DepthCrawlerPlugin plugin;
    private final Map<UUID, DungeonSession> sessions; // leader -> session
    private final Map<UUID, DungeonSession> playerSession; // player -> session
    private final Map<String, VoidChunkGenerator> generators = new ConcurrentHashMap<>(); // world名 -> 構造ジェネレーター

    public DungeonManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.sessions = new ConcurrentHashMap<>();
        this.playerSession = new ConcurrentHashMap<>();
    }

    public VoidChunkGenerator getGenerator(World world) {
        return world == null ? null : generators.get(world.getName());
    }

    public DungeonSession createSession(Player leader) {
        World world = createDungeonWorld();
        DungeonSession session = new DungeonSession(world, leader.getUniqueId());
        sessions.put(leader.getUniqueId(), session);
        playerSession.put(leader.getUniqueId(), session);
        return session;
    }

    public DungeonSession getSession(UUID uuid) {
        return playerSession.get(uuid);
    }

    public DungeonSession getSessionByLeader(UUID uuid) {
        return sessions.get(uuid);
    }

    public void joinSession(DungeonSession session, Player player) {
        session.addMember(player.getUniqueId());
        playerSession.put(player.getUniqueId(), session);
        player.teleport(getSpawn(session));
    }

    public World createDungeonWorld() {
        String name = "dungeon_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis();
        WorldCreator creator = new WorldCreator(name);
        creator.environment(World.Environment.NORMAL);
        var cfg = plugin.getDungeonConfig();
        VoidChunkGenerator generator = new VoidChunkGenerator(cfg.getGridSize(), cfg.getRoomInnerSize(),
                cfg.getFloorSpacing(), cfg.getBaseY());
        creator.generator(generator);
        creator.generateStructures(false);
        World world = Bukkit.createWorld(creator);
        if (world != null) {
            generators.put(world.getName(), generator);
            world.setGameRule(org.bukkit.GameRule.MOB_GRIEFING, plugin.getDungeonConfig().isMobGriefingEnabled());
            world.setGameRule(org.bukkit.GameRule.DO_FIRE_TICK, plugin.getDungeonConfig().isFireTickEnabled());
            // バニラの自然湧きは止め、湧き数はDifficultyManager（30+F×3）のみで管理する
            world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setTime(6000);
        }
        return world;
    }

    public org.bukkit.Location getSpawn(DungeonSession session) {
        FloorGenerator generator = new FloorGenerator(plugin);
        return generator.getSpawnLocation(session.getWorld(), session.getFloor());
    }

    public void removeSession(DungeonSession session) {
        if (session == null) {
            return;
        }
        if (!sessions.containsKey(session.getLeaderId())) {
            return;
        }
        // 既に退出済みのメンバーも含め、このセッションを指す全エントリを除去
        playerSession.values().removeIf(v -> v == session);
        sessions.remove(session.getLeaderId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = session.getWorld();
            if (world != null) {
                java.io.File folder = world.getWorldFolder();
                for (org.bukkit.entity.Player p : world.getPlayers()) {
                    p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }
                generators.remove(world.getName());
                if (Bukkit.unloadWorld(world, false)) {
                    deleteRecursively(folder);
                }
            }
        }, 40L);
    }

    /** プレイヤー単位でセッション紐付けを解除する（生還・死亡・脱退時）。 */
    public void detachPlayer(UUID uuid) {
        playerSession.remove(uuid);
    }

    private void deleteRecursively(java.io.File f) {
        if (f == null || !f.exists()) {
            return;
        }
        java.io.File[] children = f.listFiles();
        if (children != null) {
            for (java.io.File c : children) {
                deleteRecursively(c);
            }
        }
        f.delete();
    }

    public void handleDisconnect(Player player) {
        DungeonSession session = playerSession.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        session.setOnline(player.getUniqueId(), false);
        if (session.hasOnlineMember()) {
            return;
        }
        // ソロ or 全員オフライン: 5分タイマー
        session.setDisconnectTime(player.getUniqueId(), System.currentTimeMillis());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!session.isOnline(player.getUniqueId())
                    && session.getDisconnectTime(player.getUniqueId()) != null
                    && System.currentTimeMillis() - session.getDisconnectTime(player.getUniqueId()) > 5 * 60 * 1000L) {
                endByDisconnect(session, player);
            }
        }, 20L * 60 * 5);
    }

    private void endByDisconnect(DungeonSession session, Player player) {
        plugin.getGameManager().handlePlayerDeath(player);
        removeSession(session);
    }

    public void updateSession(GameState state, Player player) {
        DungeonSession session = playerSession.get(player.getUniqueId());
        if (session != null) {
            session.setState(state);
        }
    }

    public void setOnline(UUID uuid, boolean value) {
        DungeonSession session = playerSession.get(uuid);
        if (session != null) {
            session.setOnline(uuid, value);
        }
    }

    public boolean isOnline(UUID uuid) {
        DungeonSession session = playerSession.get(uuid);
        return session != null && session.isOnline(uuid);
    }
}
