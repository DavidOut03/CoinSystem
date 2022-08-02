package com.davidout.CoinSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    public int loadPlayer(UUID uuid) {
        int coins = 0;

        if(CoinAPI.getInstance().getMySQLDatabase() != null && CoinAPI.getInstance().getMySQLDatabase().isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(CoinAPI.getInstance(), new Runnable() {
                @Override
                public void run() {
                    try {
                        ResultSet result = CoinAPI.getInstance().getMySQLDatabase().query("SELECT COINS FROM coins WHERE UUID=?", uuid.toString());
                        result.next();
                        CoinAPI.setCoins(uuid, result.getInt("COINS"));
                    } catch (Exception ex) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while getting " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
                    }
                }
            });
        } else {
            if(yaml.get("Player." + uuid) != null) {
                try {
                    coins = yaml.getInt("Player." + uuid.toString());
                    CoinAPI.setCoins(uuid, coins);
                } catch (Exception ex) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while getting " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
                }
            }
        }
        return coins;
    }

    public void savePlayer(UUID uuid) {
        if(CoinAPI.getInstance().getMySQLDatabase() != null && CoinAPI.getInstance().getMySQLDatabase().isConnected()) {
            try {
                ResultSet results = CoinAPI.getInstance().getMySQLDatabase().query("SELECT * FROM coins WHERE UUID=?", uuid.toString());
                results.next();

                if(!CoinAPI.getInstance().getMySQLDatabase().dataExists("coins", "UUID", uuid.toString()) ) {
                    CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("INSERT IGNORE INTO coins (UUID) VALUES (?)", uuid.toString());
                }

                CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("UPDATE coins SET COINS=? WHERE UUID=?", CoinAPI.getCoins(uuid), uuid.toString());
                CoinAPI.deleteAccount(uuid);
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while saving " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
            }
        } else {
            try {
                yaml.set("Player." + uuid.toString(), CoinAPI.getCoins(uuid));
                yaml.save(file);
                CoinAPI.deleteAccount(uuid);
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while saving " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
            }
        }
    }
}


