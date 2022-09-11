package com.davidout.CoinSystem.data;

import com.davidout.CoinSystem.CoinAPI;
import com.davidout.CoinSystem.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.*;

public class Database {

    public static Database getInstance() {return CoinAPI.getInstance().getMySQLDatabase();}

    private String host;
    private String username;
    private String password;
    private String databaseName;

    private Connection con;

    public Connection getConnection() {
        return this.con;
    }

    public void connect() {
        try {
            long startTime = System.currentTimeMillis();
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + 3306 + "/" + databaseName + "?useSSL=false", username, password);

            long endTime = System.currentTimeMillis();
            Bukkit.getConsoleSender().sendMessage(Chat.format("&aSuccessfully connected to the database in " + (endTime - startTime) + " ms"));
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while connecting to database");
            e.printStackTrace();
        }
    }

    public ResultSet query(String query, Object... args) {
        boolean canContinue = checkConnection();
        if(!canContinue) return null;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void asyncQuery(final Callback<ResultSet> callback, String query, Object... args) {
        boolean canContinue = checkConnection();
        if(!canContinue) return;

        // This creates an async querry with the callback you can run a function after the results.
        Bukkit.getScheduler().runTaskAsynchronously(CoinAPI.getInstance(), new Runnable() {
            @Override
            public void run() {

                try {
                    PreparedStatement ps = con.prepareStatement(query);
                    for (int i = 0; i < args.length; i++) {
                        ps.setObject(i + 1, args[i]);
                    }
                    ResultSet result = ps.executeQuery();

                    if(result.next()) {
                        callback.onSuccess(result);
                        return;
                    }
                    callback.onDataNotFound();
                } catch (SQLException ex) {
                    callback.onException(ex.getCause());
                    ex.printStackTrace();
                }
            }
        });

    }

    public int update(String query, Object... args) {
        try {
            boolean canContinue = checkConnection();
            if(!canContinue) return 0;
            PreparedStatement ps = con.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void asyncUpdate(String query, Object... args) {
        Bukkit.getScheduler().runTaskAsynchronously(CoinAPI.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    boolean canContinue = checkConnection();
                    if(!canContinue) return;
                    PreparedStatement ps = con.prepareStatement(query);
                    for (int i = 0; i < args.length; i++) {
                        ps.setObject(i + 1, args[i]);
                    }
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean playerInDatabase(Player p, String databaseName) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM " + databaseName + " WHERE UUID=?");
            ps.setString(1, p.getUniqueId().toString());
            ResultSet results = ps.executeQuery();
            if(results.next()) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * reconnect if the sql server died and disconnected
     */

    public boolean checkConnection() {
        if(con == null) {
            CoinAPI.getInstance().getLogger().warning("Cannot use 'con.isClosed()' in 'checkConnection()' because 'con' is null.");
            return false;
        }

        try {
            if(con.isClosed()) {
                connect();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        if (this.con == null)
            return;
        try {
            con.close();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while disconnecting from database");
            e.printStackTrace();
        }
    }

    public void setDatabaseName(String name) {
        this.databaseName = name;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean pluginIsConnected() {
        return (this.con != null);
    }
}


