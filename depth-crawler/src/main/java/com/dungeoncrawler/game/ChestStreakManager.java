package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import org.bukkit.entity.Player;

public class ChestStreakManager {

    private final DepthCrawlerPlugin plugin;

    public ChestStreakManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    public void onChestOpened(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            return;
        }
        int streak = session.getStreak() + 1;
        session.setStreak(streak);
        player.sendMessage("§6宝箱ストリーク: §e" + streak);
        if (streak % 3 == 0) {
            player.sendMessage("§cストリークが高まると敵が強くなります！");
        }
    }

    public double getQualityBonus(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            return 0;
        }
        double multiplier = plugin.getDungeonConfig().getChestStreakMultiplier();
        double bonus = Math.min(0.5, session.getStreak() * multiplier);
        // ★幸運の発見: ドロップ品質+25%
        if (session.getUpgradeLevel(UpgradeType.LUCK) > 0) {
            bonus += 0.25;
        }
        return bonus;
    }
}
