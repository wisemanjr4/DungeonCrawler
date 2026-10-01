package com.dungeoncrawler.dungeon;

import com.dungeoncrawler.game.FloorModifier;
import com.dungeoncrawler.game.GameState;
import com.dungeoncrawler.game.UpgradeType;
import org.bukkit.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DungeonSession {

    private final UUID sessionId;
    private final World world;
    private final UUID leaderId;
    private final Set<UUID> members;
    private final Map<UUID, Boolean> online;
    private final Map<UUID, Long> disconnectTime;
    private final Map<UpgradeType, Integer> upgrades;

    private int floor;
    private FloorModifier modifier;
    private GameState state;
    private int streak;
    private final Set<UUID> mercyUsedPlayers = new HashSet<>();

    public DungeonSession(World world, UUID leaderId) {
        this.sessionId = UUID.randomUUID();
        this.world = world;
        this.leaderId = leaderId;
        this.members = new HashSet<>();
        this.online = new HashMap<>();
        this.disconnectTime = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.floor = 0;
        this.modifier = FloorModifier.random();
        this.state = GameState.EXPLORING;
        this.streak = 0;
        addMember(leaderId);
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public World getWorld() {
        return world;
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
        online.put(uuid, true);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        online.remove(uuid);
        disconnectTime.remove(uuid);
    }

    public void setOnline(UUID uuid, boolean value) {
        online.put(uuid, value);
    }

    public boolean isOnline(UUID uuid) {
        return online.getOrDefault(uuid, false);
    }

    public void setDisconnectTime(UUID uuid, Long millis) {
        if (millis == null) {
            disconnectTime.remove(uuid);
        } else {
            disconnectTime.put(uuid, millis);
        }
    }

    public Long getDisconnectTime(UUID uuid) {
        return disconnectTime.get(uuid);
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public FloorModifier getModifier() {
        return modifier;
    }

    public void setModifier(FloorModifier modifier) {
        this.modifier = modifier;
    }

    /** フロア生成中（生成完了までは出口判定・二重遷移を行わない）。 */
    private volatile boolean transitioning;

    public boolean isTransitioning() {
        return transitioning;
    }

    public void setTransitioning(boolean transitioning) {
        this.transitioning = transitioning;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = Math.max(0, streak);
    }

    public int getUpgradeLevel(UpgradeType type) {
        return upgrades.getOrDefault(type, 0);
    }

    public Map<UpgradeType, Integer> getUpgrades() {
        return upgrades;
    }

    public void addUpgrade(UpgradeType type) {
        upgrades.merge(type, 1, Integer::sum);
    }

    public boolean isRestFloor() {
        return floor > 0 && floor % 5 == 0;
    }

    public boolean isBossFloor() {
        return floor > 0 && floor % 10 == 0;
    }

    public boolean isMerciesUsed(UUID uuid) {
        return mercyUsedPlayers.contains(uuid);
    }

    public void setMerciesUsed(UUID uuid) {
        mercyUsedPlayers.add(uuid);
    }

    public boolean hasOnlineMember() {
        for (UUID uuid : members) {
            if (online.getOrDefault(uuid, false)) {
                return true;
            }
        }
        return false;
    }
}
