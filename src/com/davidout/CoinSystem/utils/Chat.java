package com.davidout.CoinSystem.utils;

import org.bukkit.ChatColor;

public class Chat {

    public static String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
