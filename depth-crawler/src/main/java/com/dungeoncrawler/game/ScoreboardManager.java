package com.dungeoncrawler.game;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {

    private final DepthCrawlerPlugin plugin;
    private final org.bukkit.scoreboard.ScoreboardManager bukkitSb;

    public ScoreboardManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.bukkitSb = plugin.getServer().getScoreboardManager();
    }

    public void update(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        Scoreboard board = bukkitSb.getNewScoreboard();
        Objective obj = board.registerNewObjective("depthcrawler", Criteria.DUMMY, "§6§l⚔ DEPTH CRAWLER ⚔");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 15;
        obj.getScore("§7┏━━━┓").setScore(line--);
        if (session != null) {
            obj.getScore("§e📊 " + session.getFloor() + "F  " + "§a通常").setScore(line--);
            obj.getScore(session.getModifier().getDisplay()).setScore(line--);
            obj.getScore("§8　").setScore(line--);
            obj.getScore("§c❤ HP: §f" + (int) player.getHealth() + "/" + (int) player.getMaxHealth()).setScore(line--);
            obj.getScore("§6⚔ コンボ: §e" + session.getStreak()).setScore(line--);
            obj.getScore("§8  ").setScore(line--);
            int rest = nextRest(session.getFloor());
            int boss = nextBoss(session.getFloor());
            obj.getScore("§7次休息: §b" + rest + "F  §7次BOSS: §c" + boss + "F").setScore(line--);
        } else {
            PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
            obj.getScore("§e📊 拠点").setScore(line--);
            obj.getScore("§8　").setScore(line--);
            obj.getScore("§6⛁ 所持金: §e" + (int) data.getBalance() + "G").setScore(line--);
            obj.getScore("§7最高到達F: §b" + data.getHighestFloor()).setScore(line--);
        }
        obj.getScore("§7━━━").setScore(0);
        player.setScoreboard(board);
    }

    private int nextRest(int floor) {
        if (floor == 0) return 5;
        return floor + (5 - (floor % 5));
    }

    private int nextBoss(int floor) {
        if (floor == 0) return 10;
        return floor + (10 - (floor % 10));
    }

    public void clear(Player player) {
        if (bukkitSb.getMainScoreboard() != null) {
            player.setScoreboard(bukkitSb.getMainScoreboard());
        }
    }
}
