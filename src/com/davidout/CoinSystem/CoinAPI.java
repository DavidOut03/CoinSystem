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

    public static HashMap<UUID, Integer> coins = new HashMap<UUID, Integer>();

    public static void addCoins(UUID uuid, int amount) {
        if (coins.get(uuid) == null) {
            coins.put(uuid, 0);
        }

        coins.put(uuid, amount + coins.get(uuid));
        updateInDatabase(uuid);
    }

    public static void removeCoins(UUID uuid, int amount) {
        if (coins.get(uuid) == null) {
            coins.put(uuid, 0);
        }
        if(coins.get(uuid) < amount) return;

        coins.put(uuid, coins.get(uuid) - amount);
        updateInDatabase(uuid);
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
        updateInDatabase(uuid);
    }

    private static void updateInDatabase(UUID uuid) {
        if(CoinAPI.getInstance().getMySQLDatabase() == null || !CoinAPI.getInstance().getMySQLDatabase().isConnected()) return;
        Database.getInstance().asyncQuery(new Callback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) throws SQLException {
                if(result == null || !result.next()) {
                    CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("INSERT IGNORE INTO coins (UUID,COINS) VALUES (?,?)", uuid.toString(), CoinAPI.getCoins(uuid));
                    return;
                }

                CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("UPDATE coins SET COINS=? WHERE UUID=?", CoinAPI.getCoins(uuid), uuid.toString());
            }

            @Override
            public void onException(Throwable cause) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There was an error while saving " + Bukkit.getOfflinePlayer(uuid).getName() + " coins. ERROR: ");
            }

            @Override
            public void onDataNotFound() {
                CoinAPI.getInstance().getMySQLDatabase().asyncUpdate("INSERT IGNORE INTO coins (UUID,COINS) VALUES (?,?)", uuid.toString(), CoinAPI.getCoins(uuid));
            }
        }, "SELECT * FROM coins WHERE UUID=?", uuid.toString());

    }


}
