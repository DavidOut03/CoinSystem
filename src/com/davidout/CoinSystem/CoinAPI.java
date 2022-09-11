package com.davidout.CoinSystem;

import com.davidout.CoinSystem.commands.CMD;
import com.davidout.CoinSystem.data.Callback;
import com.davidout.CoinSystem.data.Config;
import com.davidout.CoinSystem.data.Database;
import com.davidout.CoinSystem.events.Events;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        getCommand("coins").setTabCompleter(new CMD());



        saveDefaultConfig();
        setUpDatabaseConnection();
//        if (Bukkit.getOnlinePlayers().size() > 0) {
//            for (Player p : Bukkit.getOnlinePlayers()) {
//                Config.loadPlayer(p.getUniqueId());
//            }
//        }


    }

    @Override
    public void onDisable() {
        if(database != null) {
            database.disconnect();
        }

//        if(coins == null) return;
//        if(coins.isEmpty()) return;
//
//            for (UUID uuid : coins.keySet()) {
//                Config.savePlayer(uuid);
//            }
//
//            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "{CoinSystem} saved players coins to the database.");
    }

    public void setUpDatabaseConnection() {
        if(!getConfig().getBoolean("database.sql.enabled")) return;
            Bukkit.getConsoleSender().sendMessage(getConfig().getBoolean("database.sql.enabled") + "" + " starting database");
            String host = getConfig().getString("database.sql.host");
            String databaseName = getConfig().getString("database.sql.database");
            String username = getConfig().getString("database.sql.username");
            String password = getConfig().getString("database.sql.password");

            if(host == null || databaseName == null || username == null || password == null) return;

            database = new Database();
            database.setDatabaseName(databaseName);
            database.setHost(host);
            database.setUsername(username);
            database.setPassword(password);
            database.connect();

            if(database != null && database.pluginIsConnected())  database.asyncUpdate("CREATE TABLE IF NOT EXISTS coins (UUID VARCHAR(36), COINS INT(100), PRIMARY KEY (UUID))");
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
        if(coins.get(uuid) < amount) return;

        coins.put(uuid, coins.get(uuid) - amount);
    }

    public static int getCoins(UUID uuid) {
        if (coins.get(uuid) == null) {
            return com.davidout.CoinSystem.data.Config.getConfig().loadPlayer(uuid);
        }

        return coins.get(uuid);
    }

    public static void deleteAccount(UUID uuid) {
        coins.remove(uuid);
    }

    public static void setCoins(UUID uuid, int amount) {
        if(amount < 0) return;
        coins.put(uuid, amount);
    }

    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }




}
