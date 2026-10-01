package com.dungeoncrawler.party;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {

    private final DepthCrawlerPlugin plugin;
    private final Map<UUID, Party> partiesByLeader;
    private final Map<UUID, Party> partyByPlayer;
    private final Map<UUID, Long> inviteExpiry;
    private final Map<UUID, UUID> inviteToLeader;

    public PartyManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.partiesByLeader = new ConcurrentHashMap<>();
        this.partyByPlayer = new ConcurrentHashMap<>();
        this.inviteExpiry = new ConcurrentHashMap<>();
        this.inviteToLeader = new ConcurrentHashMap<>();
    }

    public Party getParty(UUID uuid) {
        return partyByPlayer.get(uuid);
    }

    public void invite(Player inviter, Player target) {
        Party party = partyByPlayer.get(inviter.getUniqueId());
        if (party == null) {
            party = new Party(inviter.getUniqueId());
            partiesByLeader.put(inviter.getUniqueId(), party);
            partyByPlayer.put(inviter.getUniqueId(), party);
        }
        if (!party.isLeader(inviter.getUniqueId())) {
            inviter.sendMessage("§cパーティリーダーのみ招待できます。");
            return;
        }
        if (partyByPlayer.containsKey(target.getUniqueId())) {
            inviter.sendMessage("§c" + target.getName() + " はすでにパーティに所属しています。");
            return;
        }
        inviteExpiry.put(target.getUniqueId(), System.currentTimeMillis() + 30_000L);
        inviteToLeader.put(target.getUniqueId(), inviter.getUniqueId());
        target.sendMessage("§6" + inviter.getName() + " がパーティに招待しました。§e/dungeon party accept§6 で承認。");
        inviter.sendMessage("§a" + target.getName() + " に招待を送りました（30秒以内に承認が必要）。");
    }

    public void accept(Player player) {
        Long expiry = inviteExpiry.get(player.getUniqueId());
        if (expiry == null || System.currentTimeMillis() > expiry) {
            player.sendMessage("§c招待がありません（または期限切れ）。");
            return;
        }
        inviteExpiry.remove(player.getUniqueId());
        UUID leaderId = inviteToLeader.remove(player.getUniqueId());
        Party party = leaderId != null ? partiesByLeader.get(leaderId) : null;
        if (party == null) {
            player.sendMessage("§c招待が見つかりません。");
            return;
        }
        party.addMember(player.getUniqueId());
        partyByPlayer.put(player.getUniqueId(), party);
        broadcast(party, "§6" + player.getName() + " がパーティに参加しました。");
    }

    public void leave(Player player) {
        if (plugin.getDungeonManager().getSession(player.getUniqueId()) != null) {
            player.sendMessage("§cダンジョン中はパーティを脱退できません。");
            return;
        }
        Party party = partyByPlayer.remove(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cパーティに所属していません。");
            return;
        }
        UUID newLeader = party.removeMember(player.getUniqueId());
        broadcast(party, "§6" + player.getName() + " がパーティから脱退しました。");
        if (newLeader != null) {
            // リーダー継承: パーティのリーダーを新リーダーに再登録
            partiesByLeader.remove(party.getLeaderId());
            Party newParty = new Party(newLeader);
            for (UUID uuid : party.getMembers()) {
                newParty.addMember(uuid);
                partyByPlayer.put(uuid, newParty);
            }
            partyByPlayer.put(newLeader, newParty);
            partiesByLeader.put(newLeader, newParty);
            broadcast(newParty, "§e新しいリーダー: " + Bukkit.getOfflinePlayer(newLeader).getName());
        } else {
            partiesByLeader.remove(party.getLeaderId());
        }
    }

    public List<Player> onlineMembers(Party party) {
        List<Player> result = new ArrayList<>();
        for (UUID uuid : party.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                result.add(p);
            }
        }
        return result;
    }

    public void disband(UUID uuid) {
        Party party = partiesByLeader.remove(uuid);
        if (party != null) {
            for (UUID member : party.getMembers()) {
                partyByPlayer.remove(member);
            }
        }
    }

    private void broadcast(Party party, String message) {
        for (UUID uuid : party.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(message);
            }
        }
    }
}
