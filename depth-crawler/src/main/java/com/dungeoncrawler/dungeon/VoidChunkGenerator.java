package com.dungeoncrawler.dungeon;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

/**
 * 何も生成しない虚無ワールド用ジェネレーター。
 * 迷路はすべてプラグインが配置するため、自然地形（と自然湧きの敵・動物）は不要。
 */
public class VoidChunkGenerator extends ChunkGenerator {

    @Override
    public Location getFixedSpawnLocation(World world, java.util.Random random) {
        return new Location(world, 0.5, 65, 0.5);
    }
}
