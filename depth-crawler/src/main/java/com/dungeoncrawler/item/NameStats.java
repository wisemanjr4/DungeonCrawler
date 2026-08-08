package com.dungeoncrawler.item;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class NameStats {

    private final UUID itemId;
    private double atkMult;
    private double defMult;
    private double spdMult;
    private double critBonus;
    private double hpMult;
    private double lifesteal;

    public NameStats(UUID itemId, double atkMult, double defMult, double spdMult,
                     double critBonus, double hpMult, double lifesteal) {
        this.itemId = itemId;
        this.atkMult = atkMult;
        this.defMult = defMult;
        this.spdMult = spdMult;
        this.critBonus = critBonus;
        this.hpMult = hpMult;
        this.lifesteal = lifesteal;
    }

    public static NameStats fromContainer(PersistentDataContainer container) {
        double atk = getDouble(container, ItemRegistry.ATK_MULT, 1.0);
        double def = getDouble(container, ItemRegistry.DEF_MULT, 1.0);
        double spd = getDouble(container, ItemRegistry.SPD_MULT, 1.0);
        double crit = getDouble(container, ItemRegistry.CRIT_BONUS, 0.0);
        double hp = getDouble(container, ItemRegistry.HP_MULT, 1.0);
        double ls = getDouble(container, ItemRegistry.LIFESTEAL, 0.0);
        return new NameStats(UUID.randomUUID(), atk, def, spd, crit, hp, ls);
    }

    private static double getDouble(PersistentDataContainer container, org.bukkit.NamespacedKey key, double def) {
        Double value = container.get(key, PersistentDataType.DOUBLE);
        return value == null ? def : value;
    }

    public void save(PersistentDataContainer container) {
        container.set(ItemRegistry.ATK_MULT, PersistentDataType.DOUBLE, atkMult);
        container.set(ItemRegistry.DEF_MULT, PersistentDataType.DOUBLE, defMult);
        container.set(ItemRegistry.SPD_MULT, PersistentDataType.DOUBLE, spdMult);
        container.set(ItemRegistry.CRIT_BONUS, PersistentDataType.DOUBLE, critBonus);
        container.set(ItemRegistry.HP_MULT, PersistentDataType.DOUBLE, hpMult);
        container.set(ItemRegistry.LIFESTEAL, PersistentDataType.DOUBLE, lifesteal);
    }

    public double getAtkMult() {
        return atkMult;
    }

    public double getDefMult() {
        return defMult;
    }

    public double getSpdMult() {
        return spdMult;
    }

    public double getCritBonus() {
        return critBonus;
    }

    public double getHpMult() {
        return hpMult;
    }

    public double getLifesteal() {
        return lifesteal;
    }

    public UUID getItemId() {
        return itemId;
    }
}
