package com.dungeoncrawler.command;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.game.GameManager;
import com.dungeoncrawler.game.InsuranceManager;
import com.dungeoncrawler.game.MercyManager;
import com.dungeoncrawler.game.UpgradeManager;
import com.dungeoncrawler.npc.NpcType;
import com.dungeoncrawler.party.Party;
import com.dungeoncrawler.player.InsuranceEntry;
import com.dungeoncrawler.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonCommand implements CommandExecutor, TabCompleter {

    private final DepthCrawlerPlugin plugin;

    public DungeonCommand(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です。");
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "enter", "go" -> enter(player);
            case "info", "status" -> info(player);
            case "rules", "help" -> sendHelp(player);
            case "mercy" -> mercy(player);
            case "insure" -> insure(player);
            case "claim" -> claim(player);
            case "insurance" -> insuranceStatus(player);
            case "party" -> party(player, args);
            case "setup" -> SetupCommand.handle(player, args, plugin);
            case "npc" -> handleNpc(player, args);
            case "admin" -> handleAdmin(player, args);
            case "room" -> handleRoom(player, args);
            default -> sendHelp(player);
        }
        return true;
    }

    private void enter(Player player) {
        // パーティリーダーのみ入室可能（メンバーはリーダーと一緒にTPされる）
        var party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party != null && !party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cパーティリーダーのみダンジョンに入室できます。");
            return;
        }
        plugin.getGameManager().enter(player);
    }

    private void info(Player player) {
        var session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage("§cダンジョンに参加していません。§e/dungeon enter§c で開始。");
            return;
        }
        player.sendMessage("§6§l=== ダンジョン情報 ===");
        player.sendMessage("§eフロア: §f" + session.getFloor() + "F");
        player.sendMessage("§e修飾子: " + session.getModifier().getDisplay());
        player.sendMessage("§eストリーク: §f" + session.getStreak());
        player.sendMessage("§eメンバー: ");
        for (UUID uuid : session.getMembers()) {
            player.sendMessage("  §7- " + Bukkit.getOfflinePlayer(uuid).getName());
        }
    }

    private void mercy(Player player) {
        plugin.getMercyManager().grantMercy(player);
    }

    private void insure(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            player.sendMessage("§c手持ちアイテムが必要です。");
            return;
        }
        InsuranceManager.Provider provider = InsuranceManager.Provider.ADVENTURER;
        plugin.getInsuranceManager().insure(player, hand, provider);
    }

    private void claim(Player player) {
        List<InsuranceEntry> claimable = plugin.getInsuranceManager().getClaimable(player);
        if (claimable.isEmpty()) {
            player.sendMessage("§c回収できる保険アイテムがありません。");
            return;
        }
        int claimed = 0;
        for (InsuranceEntry entry : claimable) {
            if (plugin.getInsuranceManager().claim(player, entry)) {
                claimed++;
            }
        }
        player.sendMessage("§a" + claimed + "件の保険アイテムを回収しました。");
    }

    private void insuranceStatus(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player.getUniqueId());
        List<InsuranceEntry> ins = data.getInsurance();
        player.sendMessage("§6§l=== 保険ステータス ===");
        if (ins.isEmpty()) {
            player.sendMessage("§7保険アイテムはありません。");
        }
        for (InsuranceEntry entry : ins) {
            String status = entry.isClaimed() ? "§7回収済" : (entry.isExpired() ? "§a返還可能" : "§e進行中");
            player.sendMessage("  " + entry.getProvider() + " " + status + " " + entry.getItem().getType().name());
        }
    }

    private void party(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c/dungeon party <invite|accept|leave|info>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "invite" -> {
                if (args.length < 3) {
                    player.sendMessage("§c/dungeon party invite <player>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage("§cプレイヤーが見つかりません。");
                    return;
                }
                plugin.getPartyManager().invite(player, target);
            }
            case "accept" -> plugin.getPartyManager().accept(player);
            case "leave" -> plugin.getPartyManager().leave(player);
            case "info" -> {
                Party party = plugin.getPartyManager().getParty(player.getUniqueId());
                if (party == null) {
                    player.sendMessage("§cパーティに所属していません。");
                    return;
                }
                player.sendMessage("§6§l=== パーティ ===");
                for (UUID uuid : party.getMembers()) {
                    player.sendMessage("  §7- " + Bukkit.getOfflinePlayer(uuid).getName()
                            + (party.isLeader(uuid) ? " §c(リーダー)" : ""));
                }
            }
            default -> player.sendMessage("§c不明なサブコマンドです。");
        }
    }

    private void handleNpc(Player player, String[] args) {
        if (!player.hasPermission("depthcrawler.admin")) {
            player.sendMessage(ChatColor.RED + "権限がありません。");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§c/dungeon npc <shop|safebox|dungeon|blacksmith|relief|blackmarket>");
            return;
        }
        NpcType type = NpcType.fromString(args[1]);
        if (type == null) {
            player.sendMessage("§c不明なNPC種別です。");
            return;
        }
        plugin.getNpcManager().spawnNpc(type, player.getLocation());
        player.sendMessage("§aNPCを設置しました: " + type.getDisplay());
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("depthcrawler.admin")) {
            player.sendMessage(ChatColor.RED + "権限がありません。");
            return;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("givemoney")) {
            player.sendMessage("§c/dungeon admin givemoney <player> <amount>");
            return;
        }
        if (args.length < 4) {
            player.sendMessage("§c/dungeon admin givemoney <player> <amount>");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            player.sendMessage("§cプレイヤーが見つかりません。");
            return;
        }
        try {
            double amount = Double.parseDouble(args[3]);
            plugin.getPlayerDataManager().get(target.getUniqueId()).addBalance(amount);
            player.sendMessage("§a" + amount + "G を " + target.getName() + " に付与しました。");
        } catch (NumberFormatException e) {
            player.sendMessage("§c金額が不正です。");
        }
    }

    private void handleRoom(Player player, String[] args) {
        if (!player.hasPermission("depthcrawler.admin")) {
            player.sendMessage(ChatColor.RED + "権限がありません。");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§c/dungeon room <save|list|info|delete>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "list" -> {
                player.sendMessage("§6§l=== 部屋一覧 ===");
                for (String name : plugin.getRoomManager().listRooms()) {
                    player.sendMessage("  §7- " + name);
                }
            }
            case "save" -> {
                if (args.length < 4) {
                    player.sendMessage("§c/dungeon room save <name> <normal|boss|rest>");
                    return;
                }
                var selection = plugin.getDungeonBuilder().getSelection(player.getUniqueId());
                if (selection[0] == null || selection[1] == null) {
                    player.sendMessage("§c/db pos1, pos2 で範囲を選択してください。");
                    return;
                }
                String category = args[3].toUpperCase();
                if (!category.equals("NORMAL") && !category.equals("BOSS") && !category.equals("REST")) {
                    player.sendMessage("§cカテゴリは normal|boss|rest のいずれかです。");
                    return;
                }
                var template = com.dungeoncrawler.dungeon.RoomTemplate.capture(
                        args[2], category, player.getWorld(), selection[0], selection[1]);
                plugin.getRoomManager().addRoom(template);
                player.sendMessage("§a部屋を保存しました: " + args[2]);
            }
            case "info" -> {
                if (args.length < 3) {
                    player.sendMessage("§c/dungeon room info <name>");
                    return;
                }
                var template = plugin.getRoomManager().getRoom(args[2]);
                if (template == null) {
                    player.sendMessage("§c部屋が見つかりません。");
                    return;
                }
                player.sendMessage("§6=== " + template.getName() + " ===");
                player.sendMessage("§7カテゴリ: " + template.getCategory());
                player.sendMessage("§7サイズ: " + template.getWidth() + "x" + template.getDepth());
                player.sendMessage("§7ブロック数: " + template.getBlocks().size());
            }
            case "delete" -> {
                if (args.length < 3) {
                    player.sendMessage("§c/dungeon room delete <name>");
                    return;
                }
                plugin.getRoomManager().removeRoom(args[2]);
                player.sendMessage("§a部屋を削除しました: " + args[2]);
            }
            default -> player.sendMessage("§c不明なサブコマンドです。");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== Depth Crawler ヘルプ ===");
        player.sendMessage("§e/dungeon enter §7- ダンジョン入室");
        player.sendMessage("§e/dungeon info §7- セッション情報");
        player.sendMessage("§e/dungeon rules §7- 遊び方");
        player.sendMessage("§e/dungeon mercy §7- 情けを乞う（1回/ラン）");
        player.sendMessage("§e/dungeon insure §7- 手持ちアイテムに保険");
        player.sendMessage("§e/dungeon claim §7- 保険アイテム回収");
        player.sendMessage("§e/dungeon insurance §7- 保険ステータス");
        player.sendMessage("§e/dungeon party invite <p> §7- パーティ招待");
        player.sendMessage("§e/dungeon party accept §7- 招待承認");
        player.sendMessage("§e/dungeon party leave §7- 脱退");
        player.sendMessage("§e/shop §7- ショップ");
        player.sendMessage("§e/safebox §7- セーフティボックス");
        player.sendMessage("§e/showitem §7- 装備ステータス表示");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.addAll(java.util.Arrays.asList("enter", "info", "rules", "mercy", "insure", "claim",
                    "insurance", "party", "setup", "npc", "admin", "room"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("party")) {
            result.addAll(java.util.Arrays.asList("invite", "accept", "leave", "info"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("npc")) {
            for (NpcType type : NpcType.values()) {
                result.add(type.name().toLowerCase());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("party") && args[1].equalsIgnoreCase("invite")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                result.add(online.getName());
            }
        }
        return result;
    }
}
