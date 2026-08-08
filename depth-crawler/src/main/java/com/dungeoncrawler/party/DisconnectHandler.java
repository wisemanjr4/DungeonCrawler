package com.dungeoncrawler.party;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DisconnectHandler {

    private final DepthCrawlerPlugin plugin;

    public DisconnectHandler(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * プレイヤー切断時の処理。
     */
    public void onDisconnect(Player player) {
        plugin.getDungeonManager().handleDisconnect(player);
        plugin.getPlayerDataManager().save(player.getUniqueId());
    }

    /**
     * プレイヤー再接続時の処理。PTメンバーの位置 or 切断位置にTP。
     */
    public void onReconnect(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        session.setOnline(player.getUniqueId(), true);
        Long dcTime = session.getDisconnectTime(player.getUniqueId());
        if (dcTime != null) {
            session.setDisconnectTime(player.getUniqueId(), null);
        }

        Player teleportTarget = null;
        for (UUID uuid : session.getMembers()) {
            if (uuid.equals(player.getUniqueId())) {
                continue;
            }
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                teleportTarget = member;
                break;
            }
        }
        if (teleportTarget != null) {
            player.teleport(teleportTarget.getLocation());
        } else {
            player.teleport(plugin.getDungeonManager().getSpawn(session));
        }
        player.sendMessage("§aダンジョンに復帰しました。");
    }
}
