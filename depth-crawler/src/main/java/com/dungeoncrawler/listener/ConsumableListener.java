package com.dungeoncrawler.listener;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.item.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * 消費アイテムの右クリック効果（仕様7.3）。
 * 撤退コンパス / リペアキット / バックパック / 食料パック / ポーション。
 */
public class ConsumableListener implements Listener {

    private final DepthCrawlerPlugin plugin;

    public ConsumableListener(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        String type = item.getItemMeta().getPersistentDataContainer()
                .get(ItemRegistry.CONSUMABLE, PersistentDataType.STRING);
        if (type == null) {
            return;
        }

        event.setCancelled(true);
        boolean consumed = false;
        switch (type) {
            case "REPAIR_KIT" -> {
                // /weaponly unmodify 相当: 改造解除
                if (plugin.getServer().getPluginManager().getPlugin("Weaponly") != null) {
                    player.performCommand("weaponly unmodify");
                }
                consumed = true;
            }
            case "BACKPACK" -> {
                plugin.getSafeBoxManager().open(player, 1);
                // バックパックは開くだけ（消費しない）
                return;
            }
            case "COMPASS" -> {
                // 撤退ポイント（出口）方向を表示
                var session = plugin.getDungeonManager().getSession(player.getUniqueId());
                if (session == null) {
                    player.sendMessage("§cダンジョン内でのみ使用できます。");
                    return;
                }
                var exit = new com.dungeoncrawler.dungeon.FloorGenerator(plugin)
                        .getExitLocation(session.getWorld(), session.getFloor());
                player.setCompassTarget(exit);
                player.sendMessage("§a撤退ポイントの方向をコンパスに設定しました。");
                consumed = true;
            }
            case "FOOD" -> {
                int food = player.getFoodLevel() + 10;
                player.setFoodLevel(Math.min(20, food));
                player.setSaturation(player.getSaturation() + 5);
                player.sendMessage("§a食料パックを消費しました。");
                consumed = true;
            }
            case "POTION" -> {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 8));
                player.sendMessage("§a回復しました。");
                consumed = true;
            }
            default -> {
            }
        }
        if (consumed) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand != null && hand.getAmount() > 1) {
                hand.setAmount(hand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }
}
