package com.davidout.CoinSystem.commands;

import com.davidout.CoinSystem.CoinAPI;
import com.davidout.CoinSystem.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CMD implements CommandExecutor, TabCompleter {

    private static String[] COMMANDS = { "set", "get", "reset", "add", "remove"};
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length == 0) {
            if(!(commandSender instanceof Player)) {
                commandSender.sendMessage(ChatColor.RED + "Use: /coins [set,get,add,remove,reset] [playername] [amount]");
                return true;
            }

            Player p = (Player) commandSender;
            p.sendMessage(Chat.format("§7You have §6" + CoinAPI.getCoins(p.getUniqueId()) + " §7coins."));
            return true;
        }

        if(commandSender instanceof  Player && !commandSender.isOp() && !commandSender.hasPermission("coinsystem.manage")) {
            Player p = (Player) commandSender;
                p.sendMessage(Chat.format("§7You have §6" + CoinAPI.getCoins(p.getUniqueId()) + " §7coins."));
                return false;
        }

        if(args.length == 2) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if(player == null) {
                commandSender.sendMessage(Chat.format("&c" + args[1] + " couldn't be found."));
                return false;
            }

            if(args[0].equalsIgnoreCase("get")) {
                commandSender.sendMessage(Chat.format("§e" + player.getName() + " &7has §6" + CoinAPI.getCoins(player.getUniqueId()) + " §7coins."));
                return true;
            }

            if(args[0].equalsIgnoreCase("reset")) {
                CoinAPI.setCoins(player.getUniqueId(), 0);
                commandSender.sendMessage(Chat.format("&7Successfully reset " + player.getName() + "'s coin amount."));
                return true;
            }
        }

        if(args.length == 3) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);


            if(player == null) {
                commandSender.sendMessage(Chat.format("&c" + args[1] + " couldn't be found."));
                return false;
            }

            if(!isInteger(args[2])) {
                commandSender.sendMessage(Chat.format("&c" + args[2] + " is not a number."));
                return false;
            }

            int amount = Integer.parseInt(args[2]);
            if(amount  < 0) {
                commandSender.sendMessage(Chat.format("&cSorry, but you can't use negative numbers."));
                return false;
            }

            if(args[0].equalsIgnoreCase("set")) {
                CoinAPI.setCoins(player.getUniqueId(), amount);
                commandSender.sendMessage(Chat.format("&7Successfully set &e" + player.getName() + "'s &7coin amount to &6" + amount + " &7coins."));
                return true;
            }

            if(args[0].equalsIgnoreCase("add")) {
                CoinAPI.addCoins(player.getUniqueId(), amount);
                commandSender.sendMessage(Chat.format("&7Successfully added &6" + amount + " &7coins to &e" + player.getName() + "&7."));
                return true;
            }

            if(args[0].equalsIgnoreCase("remove")) {
                if(amount > CoinAPI.getCoins(player.getUniqueId())) {
                    commandSender.sendMessage(Chat.format("&cYou can't remove more coins than the player has."));
                    return false;
                }

                CoinAPI.removeCoins(player.getUniqueId(), amount);
                commandSender.sendMessage(Chat.format("&7Successfully removed &6" + amount + " &7coins from &e" + player.getName() + "&7."));
                return true;
            }
        }

        return false;
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(!commandSender.isOp() && !commandSender.hasPermission("coinsystem.manage")) {
            COMMANDS = new String[]{};
        }

        if(args.length == 1) {
            final List<String> completions = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);

            Collections.sort(completions);
            return completions;
        } else if(args.length == 2) {
            final List<String> completions = new ArrayList<>();
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach( player -> { playerNames.add(player.getName()); });

            StringUtil.copyPartialMatches(args[1], playerNames, completions);

            Collections.sort(completions);
            return completions;
        }

        return new ArrayList<>();
    }
}



