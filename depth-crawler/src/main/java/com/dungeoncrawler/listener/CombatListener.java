package com.dungeoncrawler.listener;

import com.dungeoncrawler.DepthCrawlerPlugin;
import com.dungeoncrawler.dungeon.DungeonSession;
import com.dungeoncrawler.game.UpgradeType;
import com.dungeoncrawler.item.ItemRegistry;
import com.dungeoncrawler.item.NameStats;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

/**
 * 装備のNameStats（アフィックス補正）とアップグレードを戦闘に反映する。
 */
public class CombatListener implements Listener {

    private final DepthCrawlerPlugin plugin;
    private final Random random = new Random();

    public CombatListener(DepthCrawlerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * プレイヤーが敵を攻撃した際、武器のATK倍率・CRIT・吸血を適用。
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        NameStats stats = statsOf(hand);
        double damage = event.getDamage();

        if (stats != null) {
            damage *= stats.getAtkMult();
        }

        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session != null) {
            damage *= attackUpgradeMultiplier(session);
            // 二段攻撃: 20%で追撃
            if (session.getUpgradeLevel(UpgradeType.DOUBLE_ATTACK) > 0 && random.nextDouble() < 0.20) {
                damage *= 2.0;
            }
            // 会心
            double critChance = stats != null ? stats.getCritBonus() : 0;
            critChance += session.getUpgradeLevel(UpgradeType.CRIT) * 0.10;
            if (random.nextDouble() < critChance) {
                damage *= 1.5;
            }
            // ライフスティール
            double ls = stats != null ? stats.getLifesteal() : 0;
            ls += session.getUpgradeLevel(UpgradeType.LIFE_STEAL) * 0.05;
            if (ls > 0) {
                double heal = Math.max(1, damage * ls);
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + heal));
            }
            // ★連鎖電撃: 近くの敵2体に連鎖ダメ30%
            if (session.getUpgradeLevel(UpgradeType.CHAIN_LIGHTNING) > 0) {
                int chained = 0;
                for (var entity : target.getNearbyEntities(6, 6, 6)) {
                    if (chained >= 2) {
                        break;
                    }
                    if (entity instanceof LivingEntity living && !living.equals(player) && !living.isDead()) {
                        living.damage(damage * 0.30, player);
                        living.getWorld().strikeLightningEffect(living.getLocation());
                        chained++;
                    }
                }
            }
            // ★爆裂の一撃: 10%で爆発+範囲ダメ50%
            if (session.getUpgradeLevel(UpgradeType.EXPLOSIVE_HIT) > 0 && random.nextDouble() < 0.10) {
                target.getWorld().createExplosion(target.getLocation(), 2.0f, false, false);
                for (var entity : target.getNearbyEntities(4, 4, 4)) {
                    if (entity instanceof LivingEntity living && !living.equals(player) && !living.isDead()) {
                        living.damage(damage * 0.50, player);
                    }
                }
            }
            // ★狂戦士: キル毎ATK+5%（実装: 攻撃時に確率でバフ付与、ダメージ反映）
            if (session.getUpgradeLevel(UpgradeType.BERSERKER) > 0) {
                damage *= 1.05;
            }
            // ★アドレナリン: HP30%以下でATK+35%/SPD+20%
            if (session.getUpgradeLevel(UpgradeType.ADRENALINE) > 0
                    && player.getHealth() <= player.getMaxHealth() * 0.30) {
                damage *= 1.35;
            }
            // ★ガラスの大砲: ATK+40% / DEF-20%
            if (session.getUpgradeLevel(UpgradeType.GLASS_CANNON) > 0) {
                damage *= 1.40;
            }
            // ★吸血のオーラ: 5ブロック以内の敵からHP吸収
            if (session.getUpgradeLevel(UpgradeType.BLOOD_AURA) > 0) {
                double absorb = 0;
                for (var entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof LivingEntity living && !living.equals(player) && !living.isDead()) {
                        absorb += 1.0;
                    }
                }
                if (absorb > 0) {
                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + absorb));
                }
            }
        }

        event.setDamage(damage);
    }

    /**
     * プレイヤーが被ダメした際、防具のDEF倍率と防御アップグレードを適用。
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        double defMult = 1.0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            NameStats stats = statsOf(armor);
            if (stats != null) {
                defMult *= stats.getDefMult();
            }
        }
        DungeonSession session = plugin.getDungeonManager().getSession(player.getUniqueId());
        if (session != null) {
            int defLvl = session.getUpgradeLevel(UpgradeType.DEFENSE);
            defMult *= Math.max(0.1, 1.0 - defLvl * 0.15);
            // ★ガラスの大砲: DEF-20%
            if (session.getUpgradeLevel(UpgradeType.GLASS_CANNON) > 0) {
                defMult *= 1.20;
            }
            // 回避
            int dodgeLvl = session.getUpgradeLevel(UpgradeType.DODGE);
            if (dodgeLvl > 0 && random.nextDouble() < dodgeLvl * 0.06) {
                event.setCancelled(true);
                return;
            }
            // 反撃（EntityDamageByEntityEventのみ）
            int counterLvl = session.getUpgradeLevel(UpgradeType.COUNTER);
            if (counterLvl > 0 && event instanceof EntityDamageByEntityEvent byEntity
                    && byEntity.getDamager() instanceof LivingEntity attacker) {
                attacker.damage(event.getDamage() * 0.08 * counterLvl);
            }
        }
        event.setDamage(event.getDamage() * defMult);
    }

    private double attackUpgradeMultiplier(DungeonSession session) {
        int atkLvl = session.getUpgradeLevel(UpgradeType.ATTACK);
        return 1.0 + atkLvl * 0.25;
    }

    private NameStats statsOf(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();
        Integer isEquip = c.get(ItemRegistry.IS_EQUIPMENT, PersistentDataType.INTEGER);
        if (isEquip == null || isEquip != 1) {
            return null;
        }
        return NameStats.fromContainer(c);
    }
}
