package com.davidout.CoinSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class CoinAPI extends JavaPlugin {

    private static CoinAPI instance;
    private static Database database;

    public static CoinAPI getInstance() {
        return instance;
    }

    public Database getMySQLDatabase() {
        return database;
    }


    public Config Config;

    @Override
    public void onEnable() {
        instance = this;
        Config = new Config();



        Bukkit.getPluginManager().registerEvents(new Events(), this);
        getCommand("coins").setExecutor(new CMD());

        saveDefaultConfig();



        if(getConfig().getBoolean("database.sql.enabled")) {
            Bukkit.getConsoleSender().sendMessage(getConfig().getBoolean("database.sql.enabled") + "" + " starting database");
            String host = getConfig().getString("database.sql.host");
            String databaseName = getConfig().getString("database.sql.database");
            String username = getConfig().getString("database.sql.username");
            String password = getConfig().getString("database.sql.password");

            if(host == null || databaseName == null || username == null || password == null) return;

            database = new Database(host, 3306, databaseName, username, password);
            database.connect();
            if(database.isConnected()) {
                database.createTable("coins", "(UUID VARCHAR(100), COINS INT(100), PRIMARY KEY (UUID)");
            }
        }

        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Config.loadPlayer(p.getUniqueId());
            }
        }


    }

    @Override
    public void onDisable() {
        if (coins.size() > 0) {
            for (UUID uuid : coins.keySet()) {
                this.Config.savePlayer(uuid);
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "{CoinSystem} saved players coins to the database.");
        }

        if(database != null) {
            database.disconnect();
        }
    }

    public static HashMap<UUID, Integer> coins = new HashMap<UUID, Integer>();

    public static void addCoins(UUID uuid, int amount) {
        if (coins.get(uuid) == null) {
            coins.put(uuid, 0);
        }

        coins.put(uuid, amount + coins.get(uuid));
    }

    public static void removeCoins(UUID uuid, int amount) {
        if (coins.get(uuid) == null) {
            coins.put(uuid, 0);
        }
        coins.put(uuid, coins.get(uuid) - amount);
    }

    public static int getCoins(UUID uuid) {
        if (coins.get(uuid) == null) {
            return com.davidout.CoinSystem.Config.getConfig().loadPlayer(uuid);
        }

        return coins.get(uuid);
    }

    public static void deleteAccount(UUID uuid) {
        coins.remove(uuid);
    }

    public static void setCoins(UUID uuid, int amount) {
        coins.put(uuid, amount);
    }


}
