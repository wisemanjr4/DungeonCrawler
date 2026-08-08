package com.dungeoncrawler.party;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {

    private final UUID leaderId;
    private final List<UUID> members;

    public Party(UUID leaderId) {
        this.leaderId = leaderId;
        this.members = new ArrayList<>();
        this.members.add(leaderId);
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public boolean isLeader(UUID uuid) {
        return leaderId.equals(uuid);
    }

    public boolean contains(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        if (!members.contains(uuid)) {
            members.add(uuid);
        }
    }

    /**
     * 脱退。リーダーが抜けた場合はオンラインメンバーに継承。
     */
    public UUID removeMember(UUID uuid) {
        members.remove(uuid);
        if (uuid.equals(leaderId) && !members.isEmpty()) {
            return members.get(0);
        }
        return null;
    }

    public int size() {
        return members.size();
    }
}
