package com.davidout.CoinSystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.*;

public class Database {

    private static Database database;
    public static Database getInstance() {return database;}

    private String host;
    private String databaseName;
    private int port;
    private String user;
    private String password;
    private Connection connection;

    public Database(String host, int port,  String databaseName, String user, String password) {
        this.host = host;
        this.port = port | 3306;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
        database = this;
    }

    public Connection getConnection() {return connection;}

    public boolean isConnected() {
        return (connection == null? false : true);
    }

    public boolean connect() {
        if(isConnected()) {return true;}

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?useSSL=false", user, password);
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Successfully connected to the database.");
            return true;
        } catch (Exception ex) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Could not connect to a database.");
            return false;
        }
    }

    public boolean disconnect() {
        if(!isConnected()) {return true;}

        try {
            connection.close();
            return true;
        } catch (Exception ex) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "There was an error while disconnecting from the mysql database.");
            ex.printStackTrace();
            return false;
        }
    }

    public boolean createTable(String table, String mysqlData) {
        PreparedStatement ps;
//        (UUID VARCHAR(100), POINTS INT(100), PRIMARY KEY (UUID)
        try {
            ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " " + mysqlData + ")");
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean set(String table, String primaryKey, String primaryKeyValue, Object ...value) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE " + primaryKey + "=?");
            ps.setString(1, primaryKeyValue);
            ResultSet results = ps.executeQuery();
            results.next();

            PreparedStatement ps2 = connection.prepareStatement("INSERT IGNORE INFO " + table + " () VALUES ()");

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public ResultSet query(String query, Object... args) {
        if(this.connection == null) return null;

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update(String query, Object... args) {
        if(this.connection == null) return;

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void asyncUpdate(String query, Object... args) {
        Bukkit.getScheduler().runTaskAsynchronously(CoinAPI.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = connection.prepareStatement(query);
                    for (int i = 0; i < args.length; i++) {
                        statement.setObject(i + 1, args[i]);
                    }
                    statement.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public boolean dataExists(String table, String primaryKey, String primaryKeyValue) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + "WHERE " + primaryKey + "=?");
            ps.setString(1, primaryKeyValue);
            ResultSet results = ps.executeQuery();
            if(results.next()) {
                return true;
            }

            return false;
        } catch (Exception ex) {
            return false;
        }

    }
}
