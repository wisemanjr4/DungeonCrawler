package com.dungeoncrawler.dungeon;

import com.dungeoncrawler.DepthCrawlerPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RoomManager {

    private final DepthCrawlerPlugin plugin;
    private final File file;
    private final Map<String, RoomTemplate> rooms;

    public RoomManager(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "rooms.yml");
        this.rooms = new TreeMap<>();
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("rooms");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection roomSection = section.getConfigurationSection(key);
            if (roomSection == null) {
                continue;
            }
            String category = roomSection.getString("category", "NORMAL");
            int width = roomSection.getInt("width", 15);
            int depth = roomSection.getInt("depth", 15);
            int spawnX = roomSection.getInt("spawnX", width / 2);
            int spawnZ = roomSection.getInt("spawnZ", 2);
            int exitX = roomSection.getInt("exitX", width / 2);
            int exitZ = roomSection.getInt("exitZ", depth - 2);
            List<String> blocks = roomSection.getStringList("blocks");
            rooms.put(key, new RoomTemplate(key, category, width, depth, spawnX, spawnZ, exitX, exitZ, blocks));
        }
    }

    public void save(String name) {
        RoomTemplate template = rooms.get(name);
        if (template == null) {
            return;
        }
        YamlConfiguration cfg = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        ConfigurationSection section = cfg.createSection("rooms." + name);
        section.set("category", template.getCategory());
        section.set("width", template.getWidth());
        section.set("depth", template.getDepth());
        section.set("spawnX", template.getSpawnX());
        section.set("spawnZ", template.getSpawnZ());
        section.set("exitX", template.getExitX());
        section.set("exitZ", template.getExitZ());
        section.set("blocks", template.getBlocks());
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("rooms.yml 保存失敗: " + e.getMessage());
        }
    }

    public void addRoom(RoomTemplate template) {
        rooms.put(template.getName(), template);
        save(template.getName());
    }

    public void removeRoom(String name) {
        rooms.remove(name);
        YamlConfiguration cfg = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        cfg.set("rooms." + name, null);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("rooms.yml 保存失敗: " + e.getMessage());
        }
    }

    public RoomTemplate getRoom(String name) {
        return rooms.get(name);
    }

    public List<String> listRooms() {
        return new ArrayList<>(rooms.keySet());
    }
}
