package com.hacknslash.weaponly.listener;

import com.hacknslash.weaponly.WeaponlyPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class WeaponlyListener implements Listener {

    private final WeaponlyPlugin plugin;

    public WeaponlyListener(WeaponlyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // 武器右クリック時の特殊処理は今後拡張予定
    }
}
