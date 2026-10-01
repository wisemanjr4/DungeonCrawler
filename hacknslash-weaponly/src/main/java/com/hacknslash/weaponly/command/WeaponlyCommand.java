package com.hacknslash.weaponly.command;

import com.hacknslash.weaponly.WeaponlyPlugin;
import com.hacknslash.weaponly.data.WeaponStats;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WeaponlyCommand implements CommandExecutor, TabCompleter {

    private final WeaponlyPlugin plugin;

    public WeaponlyCommand(WeaponlyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "プレイヤーのみ実行可能です。");
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!WeaponStats.isWeapon(hand)) {
            player.sendMessage(ChatColor.RED + "手持ちの武器・防具が対象です。/weaponly info で確認してください。");
            return true;
        }
        WeaponStats.ensureInitialized(hand);

        switch (args[0].toLowerCase()) {
            case "reforge" -> reforge(player, hand, args.length > 1 && args[1].equalsIgnoreCase("precise"));
            case "awaken" -> awaken(player, hand);
            case "modify" -> modify(player, hand, args.length > 1 ? args[1] : "");
            case "unmodify" -> unmodify(player, hand);
            case "gem" -> gem(player, hand, args);
            case "synthesize" -> synthesize(player, hand);
            case "info" -> info(player, hand);
            default -> sendHelp(player);
        }
        // 変更はここで書き戻す（先に書き戻すと以降の変更が切り離されたコピーにしか入らない）
        player.getInventory().setItemInMainHand(hand);
        return true;
    }

    private void reforge(Player player, ItemStack item, boolean precise) {
        double current = WeaponStats.getBaseAtk(item);
        double max = WeaponStats.getAtkMax(item);
        if (max <= 0) {
            player.sendMessage(ChatColor.RED + "このアイテムにはリフォージ対象がありません。");
            return;
        }
        // 上昇成功率 = 100 - (現在値/最高値)*100
        double success = 100 - ((current / max) * 100);
        double cost = precise ? plugin.getConfig().getDouble("economy.reforge-precise-cost", 500)
                : plugin.getConfig().getDouble("economy.reforge-cost", 100);
        if (!pay(player, cost)) {
            player.sendMessage(ChatColor.RED + "残高が不足しています（必要: " + (int) cost + "G）。");
            return;
        }

        // 精密鍛石は下降せず必ず上昇（巻き込みなし）
        boolean up = precise || Math.random() * 100 < success;
        double before = current;

        if (up) {
            double gain = (max - current) * 0.1 + 0.5;
            current = Math.min(max, current + gain);
            WeaponStats.setBaseAtk(item, current);
            player.sendMessage(ChatColor.GREEN + "リフォージ成功! 攻撃力: " + format(before) + " → " + format(current));
        } else {
            double loss = (max - current) * 0.05 + 0.3;
            current = Math.max(0, current - loss);
            WeaponStats.setBaseAtk(item, current);
            player.sendMessage(ChatColor.RED + "リフォージ失敗... 攻撃力: " + format(before) + " → " + format(current));
        }
        // リフォージでジェム・改造は外れる（覚醒は引き継ぐ）
        WeaponStats.setModType(item, null);
        WeaponStats.clearGems(item);
        player.sendMessage(ChatColor.GRAY + "リフォージによりジェム・改造は外れました。");
        WeaponStats.updateLore(item);
    }

    private void awaken(Player player, ItemStack item) {
        int level = WeaponStats.getAwakenLevel(item);
        int maxLevel = plugin.getConfig().getInt("awaken.max", 10);
        if (level >= maxLevel) {
            player.sendMessage(ChatColor.RED + "すでに最大覚醒（+10）です。");
            return;
        }
        double cost = plugin.getConfig().getDouble("economy.awaken-base-cost", 200) + level * 100;
        if (!consumeMaterial(player)) {
            player.sendMessage(ChatColor.RED + "覚醒には素材アイテムが1個必要です。");
            return;
        }
        if (!pay(player, cost)) {
            refundMaterial(player);
            player.sendMessage(ChatColor.RED + "残高が不足しています（必要: " + (int) cost + "G）。");
            return;
        }
        int quality = Math.min(5, WeaponStats.getAwakenQuality(item) + 1);
        WeaponStats.setAwakenQuality(item, quality);
        WeaponStats.setAwakenLevel(item, level + 1);
        player.sendMessage(ChatColor.GOLD + "覚醒成功! +" + (level + 1) + " （クオリティ: " + "☆".repeat(quality) + "）");
        WeaponStats.updateLore(item);
    }

    private void modify(Player player, ItemStack item, String type) {
        List<String> types = plugin.getConfig().getStringList("mods.types");
        if (!types.contains(type)) {
            player.sendMessage(ChatColor.RED + "改造タイプは " + String.join(", ", types));
            return;
        }
        double cost = plugin.getConfig().getDouble("economy.modify-cost", 300);
        if (!pay(player, cost)) {
            player.sendMessage(ChatColor.RED + "残高が不足しています（必要: " + (int) cost + "G）。");
            return;
        }
        WeaponStats.setModType(item, type);
        player.sendMessage(ChatColor.AQUA + "改造完了: 改[" + type + "]");
        WeaponStats.updateLore(item);
    }

    private void unmodify(Player player, ItemStack item) {
        if (WeaponStats.getModType(item) == null) {
            player.sendMessage(ChatColor.RED + "改造が施されていません。");
            return;
        }
        double cost = plugin.getConfig().getDouble("economy.unmodify-cost", 0);
        if (cost > 0 && !pay(player, cost)) {
            player.sendMessage(ChatColor.RED + "残高が不足しています。");
            return;
        }
        WeaponStats.setModType(item, null);
        player.sendMessage(ChatColor.GREEN + "改造を解除しました。");
        WeaponStats.updateLore(item);
    }

    private void gem(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/weaponly gem apply <effect> | gem remove <id>");
            return;
        }
        if (args[1].equalsIgnoreCase("apply")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "/weaponly gem apply <effect>");
                return;
            }
            double cost = plugin.getConfig().getDouble("economy.gem-apply-cost", 150);
            if (!pay(player, cost)) {
                player.sendMessage(ChatColor.RED + "残高が不足しています。");
                return;
            }
            WeaponStats.addGem(item, args[2]);
            player.sendMessage(ChatColor.GREEN + "ジェム付与: " + args[2]);
            WeaponStats.updateLore(item);
        } else if (args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "/weaponly gem remove <id>");
                return;
            }
            double cost = plugin.getConfig().getDouble("economy.gem-remove-cost", 50);
            if (!pay(player, cost)) {
                player.sendMessage(ChatColor.RED + "残高が不足しています。");
                return;
            }
            try {
                int index = Integer.parseInt(args[2]);
                WeaponStats.removeGem(item, index);
                player.sendMessage(ChatColor.GREEN + "ジェムを除去しました。");
                WeaponStats.updateLore(item);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "IDは数字です。");
            }
        }
    }

    private void synthesize(Player player, ItemStack item) {
        double cost = plugin.getConfig().getDouble("economy.synthesize-cost", 250);
        if (!pay(player, cost)) {
            player.sendMessage(ChatColor.RED + "残高が不足しています。");
            return;
        }
        // 合成: 最低値引き上げ（下振れ対策）
        double current = WeaponStats.getBaseAtk(item);
        double max = WeaponStats.getAtkMax(item);
        double newMin = Math.min(max, current + (max - current) * 0.15 + 0.5);
        WeaponStats.setBaseAtk(item, newMin);
        player.sendMessage(ChatColor.GOLD + "合成完了! 最低値が引き上げられました: " + format(newMin));
        WeaponStats.updateLore(item);
    }

    private void info(Player player, ItemStack item) {
        player.sendMessage(ChatColor.GOLD + "=== 装備情報 ===");
        player.sendMessage("§7名称: " + (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName() : item.getType().name()));
        player.sendMessage("§7覚醒: +" + WeaponStats.getAwakenLevel(item) + " 品質: " + "☆".repeat(Math.min(5, WeaponStats.getAwakenQuality(item))));
        String mod = WeaponStats.getModType(item);
        player.sendMessage("§7改造: " + (mod != null ? "改[" + mod + "]" : "なし"));
        List<String> gems = WeaponStats.getGems(item);
        player.sendMessage("§7ジェム: " + (gems.isEmpty() ? "なし" : String.join(", ", gems)));
        player.sendMessage("§7基礎攻撃力: " + format(WeaponStats.getBaseAtk(item)) + " / " + format(WeaponStats.getAtkMax(item)));
        player.sendMessage("§7基礎防御力: " + format(WeaponStats.getBaseDef(item)) + " / " + format(WeaponStats.getDefMax(item)));
        String element = WeaponStats.getElementType(item);
        player.sendMessage("§7属性: " + (element != null ? element + " " + WeaponStats.getElementValue(item) : "なし"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== HACKnSLASH Weaponly ===");
        player.sendMessage("§e/weaponly reforge [precise] §7- 武器性能再抽選");
        player.sendMessage("§e/weaponly awaken §7- 素材消費で覚醒(+1〜+10)");
        player.sendMessage("§e/weaponly modify <type> §7- 改造（隼/重/烈/円/堅/鋭/魔/真）");
        player.sendMessage("§e/weaponly unmodify §7- 改造解除");
        player.sendMessage("§e/weaponly gem apply <effect> §7- ジェム付与");
        player.sendMessage("§e/weaponly gem remove <id> §7- ジェム除去");
        player.sendMessage("§e/weaponly synthesize §7- 素材合成");
        player.sendMessage("§e/weaponly info §7- 装備情報");
    }

    private boolean pay(Player player, double amount) {
        if (amount <= 0) {
            return true;
        }
        // DepthCrawler の残高とリフレクションで連携（未導入なら単体動作）
        try {
            org.bukkit.plugin.Plugin dc = player.getServer().getPluginManager().getPlugin("DepthCrawler");
            if (dc == null) {
                return true;
            }
            Object pm = dc.getClass().getMethod("getPlayerDataManager").invoke(dc);
            Object data = pm.getClass().getMethod("get", java.util.UUID.class).invoke(pm, player.getUniqueId());
            if (data == null) {
                return true;
            }
            return (Boolean) data.getClass().getMethod("deductBalance", double.class).invoke(data, amount);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().warning("DepthCrawler 連携に失敗したため操作を拒否しました: " + e);
            return false;
        }
    }

    private static final org.bukkit.NamespacedKey DC_MATERIAL = new org.bukkit.NamespacedKey("depthcrawler", "is_material");
    private ItemStack lastConsumed;

    /** DepthCrawler 導入時のみ、素材アイテムを1個消費する（未導入なら不要）。 */
    private boolean consumeMaterial(Player player) {
        lastConsumed = null;
        if (player.getServer().getPluginManager().getPlugin("DepthCrawler") == null) {
            return true;
        }
        for (ItemStack it : player.getInventory().getContents()) {
            if (it == null || !it.hasItemMeta()) continue;
            Integer v = it.getItemMeta().getPersistentDataContainer().get(DC_MATERIAL, org.bukkit.persistence.PersistentDataType.INTEGER);
            if (v != null && v == 1) {
                lastConsumed = it.clone();
                lastConsumed.setAmount(1);
                it.setAmount(it.getAmount() - 1);
                return true;
            }
        }
        return false;
    }

    private void refundMaterial(Player player) {
        if (lastConsumed != null) {
            player.getInventory().addItem(lastConsumed).values()
                    .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
            lastConsumed = null;
        }
    }

    private String format(double v) {
        return String.format("%.1f", v);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.addAll(List.of("reforge", "awaken", "modify", "unmodify", "gem", "synthesize", "info"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("modify")) {
            result.addAll(plugin.getConfig().getStringList("mods.types"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("gem")) {
            result.addAll(List.of("apply", "remove"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reforge")) {
            result.add("precise");
        }
        return result;
    }
}
