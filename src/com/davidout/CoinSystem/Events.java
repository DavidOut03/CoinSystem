package com.davidout.CoinSystem;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Config.getConfig().loadPlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Config.getConfig().savePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
       Config.getConfig().savePlayer(e.getPlayer().getUniqueId());
    }
}
