package com.hacknslash.weaponly;

import com.hacknslash.weaponly.command.WeaponlyCommand;
import com.hacknslash.weaponly.data.WeaponStats;
import com.hacknslash.weaponly.listener.WeaponlyListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class WeaponlyPlugin extends JavaPlugin {

    private static WeaponlyPlugin instance;

    public static WeaponlyPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        WeaponStats.initKeys(this);
        getCommand("weaponly").setExecutor(new WeaponlyCommand(this));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new WeaponlyListener(this), this);

        getLogger().info("HACKnSLASH Weaponly v0.1.0 が有効になりました。");
    }

    @Override
    public void onDisable() {
        getLogger().info("HACKnSLASH Weaponly が無効になりました。");
    }
}
