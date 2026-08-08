package com.dungeoncrawler;

import com.dungeoncrawler.command.BuildCommand;
import com.dungeoncrawler.command.DungeonCommand;
import com.dungeoncrawler.command.SafeBoxCommand;
import com.dungeoncrawler.command.SetupCommand;
import com.dungeoncrawler.command.ShopCommand;
import com.dungeoncrawler.command.ShowItemCommand;
import com.dungeoncrawler.config.DungeonConfig;
import com.dungeoncrawler.data.PlayerDataManager;
import com.dungeoncrawler.dungeon.DungeonBuilder;
import com.dungeoncrawler.dungeon.DungeonManager;
import com.dungeoncrawler.dungeon.RoomManager;
import com.dungeoncrawler.game.ChestStreakManager;
import com.dungeoncrawler.game.DifficultyManager;
import com.dungeoncrawler.game.ExtractionManager;
import com.dungeoncrawler.game.GameManager;
import com.dungeoncrawler.game.InsuranceManager;
import com.dungeoncrawler.game.LootManager;
import com.dungeoncrawler.game.MercyManager;
import com.dungeoncrawler.game.ScoreboardManager;
import com.dungeoncrawler.game.UpgradeManager;
import com.dungeoncrawler.item.ItemNameGenerator;
import com.dungeoncrawler.item.ItemRegistry;
import com.dungeoncrawler.listener.ChestListener;
import com.dungeoncrawler.listener.CombatListener;
import com.dungeoncrawler.listener.ConsumableListener;
import com.dungeoncrawler.listener.DungeonListener;
import com.dungeoncrawler.listener.MobDropListener;
import com.dungeoncrawler.listener.NpcListener;
import com.dungeoncrawler.mob.CustomMobManager;
import com.dungeoncrawler.npc.BlackMarketManager;
import com.dungeoncrawler.npc.BlacksmithGui;
import com.dungeoncrawler.npc.NpcManager;
import com.dungeoncrawler.npc.ReliefGui;
import com.dungeoncrawler.party.PartyManager;
import com.dungeoncrawler.player.SafeBoxManager;
import com.dungeoncrawler.shop.ShopManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DepthCrawlerPlugin extends JavaPlugin {

    private static DepthCrawlerPlugin instance;

    private DungeonConfig dungeonConfig;
    private PlayerDataManager playerDataManager;
    private DungeonManager dungeonManager;
    private GameManager gameManager;
    private LootManager lootManager;
    private ItemNameGenerator itemNameGenerator;
    private ItemRegistry itemRegistry;
    private NpcManager npcManager;
    private PartyManager partyManager;
    private SafeBoxManager safeBoxManager;
    private ShopManager shopManager;
    private InsuranceManager insuranceManager;
    private ChestStreakManager chestStreakManager;
    private DifficultyManager difficultyManager;
    private ExtractionManager extractionManager;
    private MercyManager mercyManager;
    private UpgradeManager upgradeManager;
    private ScoreboardManager scoreboardManager;
    private RoomManager roomManager;
    private CustomMobManager customMobManager;
    private DungeonBuilder dungeonBuilder;
    private BlackMarketManager blackMarketManager;
    private ReliefGui reliefGui;
    private BlacksmithGui blacksmithGui;

    public static DepthCrawlerPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.dungeonConfig = new DungeonConfig(this);

        this.playerDataManager = new PlayerDataManager(this);
        this.roomManager = new RoomManager(this);
        this.itemNameGenerator = new ItemNameGenerator();
        this.itemRegistry = new ItemRegistry(this);
        this.lootManager = new LootManager(this);
        this.dungeonManager = new DungeonManager(this);
        this.dungeonBuilder = new DungeonBuilder();
        this.partyManager = new PartyManager(this);
        this.safeBoxManager = new SafeBoxManager(this);
        this.insuranceManager = new InsuranceManager(this);
        this.chestStreakManager = new ChestStreakManager(this);
        this.difficultyManager = new DifficultyManager(this);
        this.extractionManager = new ExtractionManager(this);
        this.mercyManager = new MercyManager(this);
        this.upgradeManager = new UpgradeManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.npcManager = new NpcManager(this);
        this.blackMarketManager = new BlackMarketManager(this);
        this.reliefGui = new ReliefGui(this);
        this.blacksmithGui = new BlacksmithGui(this);
        this.customMobManager = new CustomMobManager(this);
        this.gameManager = new GameManager(this);

        registerListeners();
        registerCommands();

        getLogger().info("DepthCrawler v1.1.0 が有効になりました。");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ChestListener(this), this);
        pm.registerEvents(new DungeonListener(this), this);
        pm.registerEvents(new NpcListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new MobDropListener(this), this);
        pm.registerEvents(new ConsumableListener(this), this);
    }

    private void registerCommands() {
        getCommand("dungeon").setExecutor(new DungeonCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("safebox").setExecutor(new SafeBoxCommand(this));
        getCommand("showitem").setExecutor(new ShowItemCommand(this));
        getCommand("db").setExecutor(new BuildCommand(this));
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdown();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("DepthCrawler が無効になりました。");
    }

    public DungeonConfig getDungeonConfig() {
        return dungeonConfig;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public ItemNameGenerator getItemNameGenerator() {
        return itemNameGenerator;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public SafeBoxManager getSafeBoxManager() {
        return safeBoxManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public InsuranceManager getInsuranceManager() {
        return insuranceManager;
    }

    public ChestStreakManager getChestStreakManager() {
        return chestStreakManager;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }

    public ExtractionManager getExtractionManager() {
        return extractionManager;
    }

    public MercyManager getMercyManager() {
        return mercyManager;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }

    public CustomMobManager getCustomMobManager() {
        return customMobManager;
    }

    public DungeonBuilder getDungeonBuilder() {
        return dungeonBuilder;
    }

    public BlackMarketManager getBlackMarketManager() {
        return blackMarketManager;
    }

    public ReliefGui getReliefGui() {
        return reliefGui;
    }

    public BlacksmithGui getBlacksmithGui() {
        return blacksmithGui;
    }
}
