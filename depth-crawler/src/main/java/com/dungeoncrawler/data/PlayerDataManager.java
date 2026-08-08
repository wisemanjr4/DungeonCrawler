package com.dungeoncrawler.data;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final DepthCrawlerPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> cache;

    public PlayerDataManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.cache = new ConcurrentHashMap<>();
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    private PlayerData load(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) {
            return new PlayerData(uuid);
        }
        try {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection section = cfg.getConfigurationSection("data");
            if (section != null) {
                return PlayerData.fromSection(uuid, section);
            }
        } catch (Exception ignored) {
        }
        return new PlayerData(uuid);
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            return;
        }
        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection section = cfg.createSection("data");
        data.toSection(section);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("プレイヤーデータ保存失敗: " + uuid + " -> " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            save(uuid);
        }
    }

    public void unload(UUID uuid) {
        save(uuid);
        cache.remove(uuid);
    }
}
