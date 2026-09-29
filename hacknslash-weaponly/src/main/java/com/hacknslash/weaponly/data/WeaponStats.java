package com.hacknslash.weaponly.data;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 武器・防具の強化ステータスをPDCに永続化する。
 * 覚醒(awaken)・ジェム(gem)・改造(mod)・リフォージ値。
 */
public class WeaponStats {

    private static NamespacedKey IS_WEAPON;
    private static NamespacedKey WEAPON_ID;
    private static NamespacedKey AWAKEN_LEVEL;
    private static NamespacedKey AWAKEN_QUALITY;
    private static NamespacedKey MOD_TYPE;
    private static NamespacedKey GEMS;
    private static NamespacedKey BASE_ATK;
    private static NamespacedKey ATK_MAX;
    private static NamespacedKey BASE_DEF;
    private static NamespacedKey DEF_MAX;
    private static NamespacedKey ELEMENT_TYPE;
    private static NamespacedKey ELEMENT_VALUE;

    private WeaponStats() {
    }

    public static void initKeys(JavaPlugin plugin) {
        IS_WEAPON = new NamespacedKey(plugin, "is_weapon");
        WEAPON_ID = new NamespacedKey(plugin, "weapon_id");
        AWAKEN_LEVEL = new NamespacedKey(plugin, "awaken_level");
        AWAKEN_QUALITY = new NamespacedKey(plugin, "awaken_quality");
        MOD_TYPE = new NamespacedKey(plugin, "mod_type");
        GEMS = new NamespacedKey(plugin, "gems");
        BASE_ATK = new NamespacedKey(plugin, "base_atk");
        ATK_MAX = new NamespacedKey(plugin, "atk_max");
        BASE_DEF = new NamespacedKey(plugin, "base_def");
        DEF_MAX = new NamespacedKey(plugin, "def_max");
        ELEMENT_TYPE = new NamespacedKey(plugin, "element_type");
        ELEMENT_VALUE = new NamespacedKey(plugin, "element_value");
    }

    /**
     * DepthCrawler 生成装備を Weaponly 対象として初回初期化する。
     * DC の atk_mult 等から基礎値を導出する。未初期化なら通常の初期化。
     */
    public static void ensureInitialized(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();
        Integer tagged = c.get(IS_WEAPON, PersistentDataType.INTEGER);
        if (tagged != null && tagged == 1 && c.has(BASE_ATK, PersistentDataType.DOUBLE)) {
            return; // すでに初期化済み（基礎攻撃力が0まで下がっても再初期化しない）
        }
        // DC の PDC から atk_mult を読み取る（DepthCrawler の namespace は小文字）
        NamespacedKey dcAtk = new NamespacedKey("depthcrawler", "atk_mult");
        Double atkMult = c.get(dcAtk, PersistentDataType.DOUBLE);
        if (atkMult == null) {
            atkMult = 5.0;
        }
        double base = atkMult;
        double max = base * 1.5;
        initialize(item, base, max, base * 0.5, base * 0.75);
    }

    /** Weaponly 初期化済み（強化対象として登録済み）か。 */
    public static boolean isTagged(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        Integer v = item.getItemMeta().getPersistentDataContainer().get(IS_WEAPON, PersistentDataType.INTEGER);
        return v != null && v == 1;
    }

    public static boolean isWeapon(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        // Weaponly 独自タグ
        if (item.hasItemMeta()) {
            Integer v = item.getItemMeta().getPersistentDataContainer().get(IS_WEAPON, PersistentDataType.INTEGER);
            if (v != null && v == 1) {
                return true;
            }
        }
        // DepthCrawler 生成装備（is_equipment）を認識
        if (item.hasItemMeta()) {
            NamespacedKey dcEquipment = new NamespacedKey("depthcrawler", "is_equipment");
            Integer v = item.getItemMeta().getPersistentDataContainer().get(dcEquipment, PersistentDataType.INTEGER);
            if (v != null && v == 1) {
                return true;
            }
        }
        // 素材ベースの判定（剣・斧・防具）
        String name = item.getType().name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }

    public static void initialize(ItemStack item, double baseAtk, double atkMax, double baseDef, double defMax) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer c = meta.getPersistentDataContainer();
        c.set(IS_WEAPON, PersistentDataType.INTEGER, 1);
        c.set(WEAPON_ID, PersistentDataType.STRING, UUID.randomUUID().toString());
        c.set(AWAKEN_LEVEL, PersistentDataType.INTEGER, 0);
        c.set(AWAKEN_QUALITY, PersistentDataType.INTEGER, 0);
        c.set(BASE_ATK, PersistentDataType.DOUBLE, baseAtk);
        c.set(ATK_MAX, PersistentDataType.DOUBLE, atkMax);
        c.set(BASE_DEF, PersistentDataType.DOUBLE, baseDef);
        c.set(DEF_MAX, PersistentDataType.DOUBLE, defMax);
        item.setItemMeta(meta);
    }

    public static int getAwakenLevel(ItemStack item) {
        Integer v = item.getItemMeta().getPersistentDataContainer().get(AWAKEN_LEVEL, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public static void setAwakenLevel(ItemStack item, int level) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(AWAKEN_LEVEL, PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }

    public static int getAwakenQuality(ItemStack item) {
        Integer v = item.getItemMeta().getPersistentDataContainer().get(AWAKEN_QUALITY, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public static void setAwakenQuality(ItemStack item, int quality) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(AWAKEN_QUALITY, PersistentDataType.INTEGER, quality);
        item.setItemMeta(meta);
    }

    public static String getModType(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(MOD_TYPE, PersistentDataType.STRING);
    }

    public static void setModType(ItemStack item, String mod) {
        ItemMeta meta = item.getItemMeta();
        if (mod == null) {
            meta.getPersistentDataContainer().remove(MOD_TYPE);
        } else {
            meta.getPersistentDataContainer().set(MOD_TYPE, PersistentDataType.STRING, mod);
        }
        item.setItemMeta(meta);
    }

    public static List<String> getGems(ItemStack item) {
        String raw = item.getItemMeta().getPersistentDataContainer().get(GEMS, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        return result;
    }

    public static void addGem(ItemStack item, String gem) {
        List<String> gems = getGems(item);
        if (gems.size() >= 3) {
            return;
        }
        gems.add(gem);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(GEMS, PersistentDataType.STRING, String.join(",", gems));
        item.setItemMeta(meta);
    }

    public static void clearGems(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(GEMS);
        item.setItemMeta(meta);
    }

    public static void removeGem(ItemStack item, int index) {
        List<String> gems = getGems(item);
        if (index < 0 || index >= gems.size()) {
            return;
        }
        gems.remove(index);
        ItemMeta meta = item.getItemMeta();
        if (gems.isEmpty()) {
            meta.getPersistentDataContainer().remove(GEMS);
        } else {
            meta.getPersistentDataContainer().set(GEMS, PersistentDataType.STRING, String.join(",", gems));
        }
        item.setItemMeta(meta);
    }

    public static double getBaseAtk(ItemStack item) {
        Double v = item.getItemMeta().getPersistentDataContainer().get(BASE_ATK, PersistentDataType.DOUBLE);
        return v == null ? 0 : v;
    }

    public static double getAtkMax(ItemStack item) {
        Double v = item.getItemMeta().getPersistentDataContainer().get(ATK_MAX, PersistentDataType.DOUBLE);
        return v == null ? 0 : v;
    }

    public static void setBaseAtk(ItemStack item, double value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(BASE_ATK, PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
    }

    public static double getBaseDef(ItemStack item) {
        Double v = item.getItemMeta().getPersistentDataContainer().get(BASE_DEF, PersistentDataType.DOUBLE);
        return v == null ? 0 : v;
    }

    public static double getDefMax(ItemStack item) {
        Double v = item.getItemMeta().getPersistentDataContainer().get(DEF_MAX, PersistentDataType.DOUBLE);
        return v == null ? 0 : v;
    }

    public static void setBaseDef(ItemStack item, double value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(BASE_DEF, PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
    }

    public static String getElementType(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(ELEMENT_TYPE, PersistentDataType.STRING);
    }

    public static void setElement(ItemStack item, String element, int value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ELEMENT_TYPE, PersistentDataType.STRING, element);
        meta.getPersistentDataContainer().set(ELEMENT_VALUE, PersistentDataType.INTEGER, value);
        item.setItemMeta(meta);
    }

    public static int getElementValue(ItemStack item) {
        Integer v = item.getItemMeta().getPersistentDataContainer().get(ELEMENT_VALUE, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    /**
     * 装備情報をロアに反映。
     */
    public static void updateLore(ItemStack item) {
        if (!isWeapon(item)) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setDisplayName(buildDisplayName(item, meta.hasDisplayName() ? meta.getDisplayName() : null));

        int level = getAwakenLevel(item);
        int quality = getAwakenQuality(item);
        if (quality > 0) {
            lore.add("§7覚醒クオリティ: §b" + "☆".repeat(Math.min(5, quality)));
        }
        if (level > 0) {
            lore.add("§7覚醒: §e+" + level);
        }
        String mod = getModType(item);
        if (mod != null) {
            lore.add("§7改造: §a改[" + mod + "]");
        }
        List<String> gems = getGems(item);
        if (!gems.isEmpty()) {
            lore.add("§7ジェム: " + String.join("§7, ", gems));
        }
        String element = getElementType(item);
        if (element != null) {
            lore.add("§7属性: §c" + element + " §e" + getElementValue(item));
        }
        lore.add("§7基礎攻撃力: §c" + format(getBaseAtk(item)) + " / " + format(getAtkMax(item)));
        lore.add("§7基礎防御力: §7" + format(getBaseDef(item)) + " / " + format(getDefMax(item)));

        List<String> existing = meta.getLore();
        List<String> kept = new ArrayList<>();
        if (existing != null) {
            for (String line : existing) {
                if (line.contains("改[") || line.contains("覚醒") || line.contains("ジェム")
                        || line.contains("属性:") || line.contains("基礎攻撃力") || line.contains("基礎防御力")) {
                    continue;
                }
                kept.add(line);
            }
        }
        kept.addAll(lore);
        meta.setLore(kept);
        item.setItemMeta(meta);
    }

    private static final String[] ROMAN = {"", "Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ"};
    private static final java.util.regex.Pattern NAME_SUFFIX =
            java.util.regex.Pattern.compile("(?:§.)*( [ⅠⅡⅢⅣⅤ])?( \\+\\d+)?( 改\\[[^\\]]*\\])?(?:§.)*$");

    /** 表記例: 炎帝剣フレアゼード Ⅴ +10 改[隼] */
    private static String buildDisplayName(ItemStack item, String current) {
        if (current == null) {
            return null;
        }
        String base = NAME_SUFFIX.matcher(current).replaceFirst("");
        StringBuilder sb = new StringBuilder(base);
        int q = Math.min(5, getAwakenQuality(item));
        int lv = getAwakenLevel(item);
        String mod = getModType(item);
        if (q > 0) sb.append(' ').append(ROMAN[q]);
        if (lv > 0) sb.append(" +").append(lv);
        if (mod != null) sb.append(" 改[").append(mod).append(']');
        return sb.toString();
    }

    private static String format(double v) {
        return String.format("%.1f", v);
    }
}
