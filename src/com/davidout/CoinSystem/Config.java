package com.davidout.CoinSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

public class Config {

    private static Config config;
    private final File file;
    private final YamlConfiguration yaml;
    public Config() {
        config = this;

        this.file = new File(CoinAPI.getInstance().getDataFolder(), "data");
        this.yaml = YamlConfiguration.loadConfiguration(file);

        if(!file.exists()) {
            try {
                yaml.createSection("Player");
                yaml.save(file);
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while setting up the files. ERROR: " + ex.getMessage());
            }
        }
    }

    public static Config getConfig() {
        return config;
    }

    public int getCoinsFromConfig(UUID uuid) {
        try {
            return yaml.getInt("Player." + uuid.toString());
        } catch (Exception ex) {
            return 0;
        }
    }
    public void loadPlayer(UUID uuid) {
        int coins = 0;
        if(yaml.get("Player." + uuid) != null) {
            try {
                coins = yaml.getInt("Player." + uuid.toString());
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while getting " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
            }
        }

        if (coins == 0) {
            CoinAPI.setCoins(uuid, 0);
            return;
        }
        CoinAPI.setCoins(uuid, coins);
    }

    public void savePlayer(UUID uuid) {

        try {
            yaml.set("Player." + uuid.toString(), CoinAPI.getCoins(uuid));
            yaml.save(file);
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while saving " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
        }

        CoinAPI.deleteAccount(uuid);
    }
}


