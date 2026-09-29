package com.dungeoncrawler.listener;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.game.GameManager;
import com.dungeoncrawler.game.GameState;
import com.dungeoncrawler.game.UpgradeManager;
import com.dungeoncrawler.party.DisconnectHandler;
import com.dungeoncrawler.player.PlayerData;
import com.dungeoncrawler.shop.ShopManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DungeonListener implements Listener {

    private final DepthCrawlerPlugin plugin;
    private final DisconnectHandler disconnectHandler;
    private final java.util.Map<UUID, Long> dynamicSpawnCooldown = new java.util.concurrent.ConcurrentHashMap<>();

    public DungeonListener(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.disconnectHandler = new DisconnectHandler(plugin);
    }

    private boolean inDungeon(Player player) {
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        return session != null && session.getWorld() != null
                && player.getWorld().equals(session.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (inDungeon(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (inDungeon(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (inDungeon(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (!inDungeon(event.getPlayer())) {
            return;
        }
        if (event.getNewGameMode() == GameMode.CREATIVE || event.getNewGameMode() == GameMode.SPECTATOR) {
            if (!event.getPlayer().hasPermission("depthcrawler.admin")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!inDungeon(event.getPlayer())) {
            return;
        }
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                || cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!inDungeon(player)) {
            return;
        }
        event.setKeepInventory(true);
        event.getDrops().clear();
        plugin.getDungeonManager().setOnline(player.getUniqueId(), false);
        plugin.getGameManager().handlePlayerDeath(player);
    }

    @EventHandler
    public void onRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // ダンジョンワールド由来の死亡・セッション残存の両方に対応
        if (player.getWorld().getName().startsWith("dungeon_")
                || plugin.getDungeonManager().getSession(player.getUniqueId()) != null) {
            plugin.getGameManager().handleRespawn(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        Player player = event.getPlayer();
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null || event.getClickedBlock() == null) {
            return;
        }

        // 休息フロアの石ボタン → アップグレードGUI
        if (event.getClickedBlock().getType() == Material.STONE_BUTTON && session.isRestFloor()) {
            event.setCancelled(true);
            plugin.getUpgradeManager().openUpgradeGui(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory inv = event.getView().getTopInventory();
        if (inv == null) {
            return;
        }
        String title = event.getView().getTitle();

        // アップグレードGUI
        if (title.equals(UpgradeManager.UPGRADE_GUI_TITLE)) {
            event.setCancelled(true);
            plugin.getUpgradeManager().handleClick(player, event.getCurrentItem());
            return;
        }
        // ショップ
        if (title.equals(ShopManager.SHOP_TITLE)) {
            event.setCancelled(true);
            handleShopMenuClick(player, event.getCurrentItem());
            return;
        }
        if (title.equals(ShopManager.BUY_TITLE)) {
            event.setCancelled(true);
            plugin.getShopManager().handleBuyClick(player, event.getCurrentItem());
            return;
        }
        if (title.equals(ShopManager.SELL_TITLE)) {
            event.setCancelled(true);
            if (event.getSlot() == 44 && event.getCurrentItem() != null) {
                plugin.getShopManager().sellAllSellables(player);
            }
            return;
        }
        // セーフボックス
        if (title.startsWith(com.dungeoncrawler.player.SafeBoxManager.TITLE)) {
            event.setCancelled(true);
            handleSafeBoxClick(player, event);
            return;
        }
        // ブラックマーケット
        if (title.equals(com.dungeoncrawler.npc.BlackMarketManager.TITLE)) {
            event.setCancelled(true);
            plugin.getBlackMarketManager().handleBuyClick(player, event.getCurrentItem());
            return;
        }
        // 救助班
        if (title.equals(com.dungeoncrawler.npc.ReliefGui.TITLE)
                || title.equals(com.dungeoncrawler.npc.ReliefGui.PROVIDER_TITLE)) {
            event.setCancelled(true);
            plugin.getReliefGui().handleClick(player, event.getCurrentItem());
            return;
        }
        // 鍛冶屋
        if (title.equals(com.dungeoncrawler.npc.BlacksmithGui.TITLE)) {
            event.setCancelled(true);
            plugin.getBlacksmithGui().handleClick(player, event.getCurrentItem());
        }
    }

    private void handleShopMenuClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }
        String name = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name.equals("購入")) {
            plugin.getShopManager().openBuyMenu(player);
        } else if (name.equals("売却")) {
            plugin.getShopManager().openSellMenu(player);
        }
    }

    private void handleSafeBoxClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 45) {
            int page = plugin.getSafeBoxManager().getOpenPage(player);
            plugin.getSafeBoxManager().open(player, page - 1);
        } else if (slot == 53) {
            int page = plugin.getSafeBoxManager().getOpenPage(player);
            plugin.getSafeBoxManager().open(player, page + 1);
        } else if (slot == 48) {
            plugin.getSafeBoxManager().deposit(player);
            plugin.getSafeBoxManager().open(player, plugin.getSafeBoxManager().getOpenPage(player));
        } else if (slot < 45 && event.getCurrentItem() != null) {
            plugin.getSafeBoxManager().withdraw(player, slot);
            plugin.getSafeBoxManager().open(player, plugin.getSafeBoxManager().getOpenPage(player));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session == null || !player.getWorld().equals(session.getWorld())) {
            return;
        }
        // 動的湧き（2秒ごと1/15）— 移動イベントが高頻度なためクールダウン制御
        long now = System.currentTimeMillis();
        Long last = dynamicSpawnCooldown.get(player.getUniqueId());
        if (last == null || now - last > 2000L) {
            dynamicSpawnCooldown.put(player.getUniqueId(), now);
            plugin.getCustomMobManager().dynamicSpawn(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 切断中だったメンバーのみ再接続処理の対象（入室直後の誤表示を防ぐ）
        DungeonSession joinSession = plugin.getDungeonManager().getSession(player.getUniqueId());
        boolean wasDisconnected = joinSession != null && !joinSession.isOnline(player.getUniqueId());
        plugin.getDungeonManager().setOnline(player.getUniqueId(), true);
        // ダンジョン内でログアウト後にサーバー再起動等でセッションが消えていた場合は拠点へ戻す
        var data = plugin.getPlayerDataManager().get(player.getUniqueId());
        if (data.isInDungeon() && plugin.getDungeonManager().getSession(player.getUniqueId()) == null) {
            data.setInDungeon(false);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.teleport(plugin.getGameManager().getDefaultHub());
                    player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    player.sendMessage("§eダンジョンが消滅していたため拠点に戻りました。");
                }
            }, 1L);
        }
        // 再接続処理
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (wasDisconnected && player.isOnline()
                    && plugin.getDungeonManager().getSession(player.getUniqueId()) != null) {
                disconnectHandler.onReconnect(player);
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        disconnectHandler.onDisconnect(event.getPlayer());
    }
}
