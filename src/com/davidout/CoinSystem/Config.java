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
    public void loadPlayer(UUID uuid) {
        int coins = 0;

        if(CoinAPI.getInstance().getMySQLDatabase() != null && CoinAPI.getInstance().getMySQLDatabase().isConnected()) {
            try {
                PreparedStatement ps = CoinAPI.getInstance().getMySQLDatabase().getConnection().prepareStatement("SELECT COINS FROM coins WHERE UUID=?");
                ps.setString(1, uuid.toString());
                ResultSet result = ps.executeQuery();
                if(result.next()) {
                    coins = result.getInt("COINS");
                }
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while getting " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
            }

            return;
        } else {
            if(yaml.get("Player." + uuid) != null) {
                try {
                    coins = yaml.getInt("Player." + uuid.toString());
                } catch (Exception ex) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while getting " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
                }
            }
        }

        if (coins == 0) {
            CoinAPI.setCoins(uuid, 0);
            return;
        }
        CoinAPI.setCoins(uuid, coins);
    }

    public void savePlayer(UUID uuid) {
        if(CoinAPI.getInstance().getMySQLDatabase() != null && CoinAPI.getInstance().getMySQLDatabase().isConnected()) {
            try {
                PreparedStatement ps = CoinAPI.getInstance().getMySQLDatabase().getConnection().prepareStatement("SELECT * FROM coins WHERE UUID=?");
                ps.setString(1, uuid.toString());
                ResultSet results = ps.executeQuery();
                results.next();

                if(!CoinAPI.getInstance().getMySQLDatabase().dataExists("coins", "UUID", uuid.toString()) ) {
                    PreparedStatement ps2 = CoinAPI.getInstance().getMySQLDatabase().getConnection().prepareStatement("INSERT IGNORE INTO coins"
                    + " (UUID) VALUES (?)");
                    ps2.setString(1, uuid.toString());
                    ps2.executeUpdate();
                }

                PreparedStatement ps3 = CoinAPI.getInstance().getMySQLDatabase().getConnection().prepareStatement("UPDATE coins SET COINS=? WHERE UUID=?");
                ps3.setInt(1, CoinAPI.getCoins(uuid));
                ps3.setString(2, uuid.toString());
                ps3.executeUpdate();

                return;
            } catch (Exception ex) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while saving " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
                return;
            }
        }

        try {
            yaml.set("Player." + uuid.toString(), CoinAPI.getCoins(uuid));
            yaml.save(file);
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while saving " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: " + ex.getMessage());
        }

        CoinAPI.deleteAccount(uuid);
    }
}


