package com.davidout.CoinSystem.events;

import com.davidout.CoinSystem.data.Config;
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

//    @EventHandler
//    public  void onClick(PlayerInteractEvent e) {
//        if(e.getClickedBlock() != null) {
//            CoinAPI.addCoins(e.getPlayer().getUniqueId(), 1000);
//            e.getPlayer().sendMessage(ChatColor.GREEN + "You received 1 coin. " + CoinAPI.getCoins(e.getPlayer().getUniqueId()));
//        }
//    }
}
