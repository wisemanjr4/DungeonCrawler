package com.dungeoncrawler;

import com.dungeoncrawler.game.DifficultyManager;
import com.dungeoncrawler.game.FloorModifier;
import com.dungeoncrawler.item.ItemGenerationResult;
import com.dungeoncrawler.item.ItemNameGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {

    @Test
    void spawnCountFollowsSpec() {
        DifficultyManager d = new DifficultyManager(null);
        assertEquals(33, d.spawnCount(1, FloorModifier.TREASURE));
        assertEquals(60, d.spawnCount(10, FloorModifier.TREASURE));
        assertEquals(180, d.spawnCount(50, FloorModifier.TREASURE));
        assertEquals(90, d.spawnCount(10, FloorModifier.SWARM));
    }

    @Test
    void eliteChanceGrowsWithPhase() {
        DifficultyManager d = new DifficultyManager(null);
        assertEquals(0.10, d.eliteChance(1), 1e-9);
        assertTrue(d.eliteChance(55) > d.eliteChance(15));
    }

    @Test
    void mobPoolNeverEmpty() {
        DifficultyManager d = new DifficultyManager(null);
        for (int f = 1; f <= 80; f++) {
            assertFalse(d.mobPoolForFloor(f).isEmpty(), "floor " + f);
        }
    }

    @Test
    void bossPoolExcludesEnderDragon() {
        assertFalse(DifficultyManager.BOSS_POOL.contains(org.bukkit.entity.EntityType.ENDER_DRAGON));
        assertEquals(9, DifficultyManager.BOSS_POOL.size());
        DifficultyManager d = new DifficultyManager(null);
        for (int i = 0; i < 500; i++) {
            assertTrue(DifficultyManager.BOSS_POOL.contains(d.randomBoss()));
        }
    }

    @Test
    void modifierMultipliersMatchSpec() {
        assertEquals(1.3, FloorModifier.BERSERK.getAtkMult());
        assertEquals(1.25, FloorModifier.FORTIFIED.getDefMult());
        assertEquals(1.2, FloorModifier.HASTE.getSpdMult());
        assertEquals(1.3, FloorModifier.PLAGUE.getDropMult());
        assertEquals(1.25, FloorModifier.WEAKNESS.getDropMult());
    }

    @Test
    void armorNameUsesRequestedMaterialPrefix() {
        ItemNameGenerator g = new ItemNameGenerator();
        String[] wrong = {"鋼鉄の", "白金の", "水晶の", "金剛の", "ミスリルの", "オリハルコンの", "黒曜の"};
        for (int i = 0; i < 500; i++) {
            ItemGenerationResult r = g.generateArmor("革の");
            assertFalse(r.getName().isBlank());
            for (String w : wrong) {
                assertFalse(r.getName().contains(w), r.getName());
            }
        }
    }

    @Test
    void generatedItemsHaveSaneMultipliers() {
        ItemNameGenerator g = new ItemNameGenerator();
        for (int i = 0; i < 500; i++) {
            for (ItemGenerationResult r : new ItemGenerationResult[]{g.generateWeapon(), g.generateArmor(), g.generateNamedWeapon(), g.generateNamedArmor()}) {
                assertFalse(r.getName().isBlank());
                assertTrue(r.getAtkMult() > 0 && r.getDefMult() > 0 && r.getSpdMult() > 0 && r.getHpMult() > 0, r.getName());
            }
        }
    }
}
