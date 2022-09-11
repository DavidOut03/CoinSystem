package com.davidout.CoinSystem.data;

import com.davidout.CoinSystem.CoinAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
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

            Database.getInstance().asyncQuery(new Callback<ResultSet>() {
                @Override
                public void onSuccess(ResultSet result) throws SQLException {
                    if(result == null || !result.next() || result.getString("COINS") == null || result.getInt("COINS") == 0) {
                        CoinAPI.setCoins(uuid, 0);
                        return;
                    }

                    CoinAPI.setCoins(uuid, result.getInt("COINS"));
                }

                @Override
                public void onException(Throwable cause) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while getting " + Bukkit.getOfflinePlayer(uuid).getName() + " coins.");
                }

                @Override
                public void onDataNotFound() {
                    CoinAPI.setCoins(uuid, 0);
                }
            }, "SELECT COINS FROM coins WHERE UUID=?", uuid.toString());
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
            Database.getInstance().asyncQuery(new Callback<ResultSet>() {
                @Override
                public void onSuccess(ResultSet result) throws SQLException {
                    if(result == null || !result.next()) {
                        CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("INSERT IGNORE INTO coins (UUID,COINS) VALUES (?,?)", uuid.toString(), CoinAPI.getCoins(uuid));
                        CoinAPI.deleteAccount(uuid);
                        return;
                    }

                    CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("UPDATE coins SET COINS=? WHERE UUID=?", CoinAPI.getCoins(uuid), uuid.toString());
                    CoinAPI.deleteAccount(uuid);
                }

                @Override
                public void onException(Throwable cause) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while saving " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ");
                }

                @Override
                public void onDataNotFound() {
                    CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("INSERT IGNORE INTO coins (UUID,COINS) VALUES (?,?)", uuid.toString(), CoinAPI.getCoins(uuid));
                    CoinAPI.deleteAccount(uuid);
                }
            }, "SELECT * FROM coins WHERE UUID=?", uuid.toString());
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


