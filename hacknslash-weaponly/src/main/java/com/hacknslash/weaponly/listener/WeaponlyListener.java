package com.hacknslash.weaponly.listener;

import com.hacknslash.weaponly.WeaponlyPlugin;
import com.hacknslash.weaponly.data.WeaponStats;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 覚醒・改造・ジェム・属性・基礎値を戦闘に反映する。
 * DepthCrawler 側の倍率（HIGH）の後に適用するため HIGHEST で動作する。
 */
public class WeaponlyListener implements Listener {

    private final WeaponlyPlugin plugin;
    /** 円（範囲）改造などの二次ダメージで本ハンドラが再入しないためのガード。 */
    private boolean processing = false;

    public WeaponlyListener(WeaponlyPlugin plugin) {
        this.plugin = plugin;
    }

    private static boolean tagged(ItemStack item) {
        return item != null && item.hasItemMeta() && WeaponStats.isTagged(item);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (processing || !(event.getDamager() instanceof Player player)
                || !(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!tagged(hand)) {
            return;
        }
        processing = true;
        try {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            double damage = event.getDamage();
            double mult = 1.0;
            double flat = WeaponStats.getBaseAtk(hand) * 0.5 + WeaponStats.getAwakenLevel(hand) * 0.5;
            double critChance = 0;
            double lifesteal = 0;

            String mod = WeaponStats.getModType(hand);
            if (mod != null) {
                switch (mod) {
                    case "重" -> mult *= 1.15;
                    case "烈" -> critChance += 0.20;
                    case "鋭" -> mult *= 1.10; // 貫通: 防御を無視する分を火力に換算
                    case "魔" -> flat += WeaponStats.getElementValue(hand) * 0.2 + 1.0;
                    case "真" -> mult *= 1.05;
                    case "隼" -> player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 0, false, false));
                    case "円" -> splash(player, target, damage * 0.30 + flat * 0.3);
                    default -> { }
                }
            }
            // 属性値（改造「魔」以外でも小さく反映）
            if (!"魔".equals(mod)) {
                flat += WeaponStats.getElementValue(hand) * 0.1;
            }
            for (String gem : WeaponStats.getGems(hand)) {
                switch (gem.toLowerCase()) {
                    case "atk", "attack", "攻撃" -> mult *= 1.10;
                    case "crit", "会心" -> critChance += 0.10;
                    case "life", "lifesteal", "吸血" -> lifesteal += 0.03;
                    case "spd", "speed", "速度" ->
                            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 0, false, false));
                    default -> { }
                }
            }

            damage = (damage + flat) * mult;
            if (critChance > 0 && rnd.nextDouble() < critChance) {
                damage *= 1.5;
            }
            if (lifesteal > 0) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + Math.max(0.5, damage * lifesteal)));
            }
            event.setDamage(damage);
        } finally {
            processing = false;
        }
    }

    private void splash(Player player, LivingEntity target, double amount) {
        for (var e : target.getNearbyEntities(3, 2, 3)) {
            if (e instanceof LivingEntity living && !(living instanceof Player) && !living.isDead()) {
                living.damage(amount, player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        double def = 0;
        double mult = 1.0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (!tagged(armor)) {
                continue;
            }
            def += WeaponStats.getBaseDef(armor) * 0.02 + WeaponStats.getAwakenLevel(armor) * 0.005;
            if ("堅".equals(WeaponStats.getModType(armor))) {
                mult *= 0.90;
            }
            if ("真".equals(WeaponStats.getModType(armor))) {
                mult *= 0.97;
            }
            List<String> gems = WeaponStats.getGems(armor);
            for (String gem : gems) {
                if (gem.equalsIgnoreCase("def") || gem.equals("防御")) {
                    mult *= 0.95;
                }
            }
        }
        if (def > 0 || mult < 1.0) {
            double reduction = Math.min(0.60, def);
            event.setDamage(event.getDamage() * (1.0 - reduction) * mult);
        }
    }
}
