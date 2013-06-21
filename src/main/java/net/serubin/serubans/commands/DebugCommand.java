package net.serubin.serubans.commands;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.serubin.serubans.SeruBans;
import net.serubin.serubans.util.DatabaseCache;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand implements CommandExecutor {

    private SeruBans plugin;

    public DebugCommand(SeruBans plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("serubans")) {
            if (sender.hasPermission(SeruBans.DEBUGPERM) || sender.isOp()
                    || (!(sender instanceof Player))) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.GOLD + "Serubans "
                            + ChatColor.YELLOW + " version "
                            + SeruBans.getVersion());
                    sender.sendMessage(ChatColor.YELLOW + "Type "
                            + ChatColor.GOLD + "'/serubans ?' "
                            + ChatColor.YELLOW + "for more debug options");
                    return true;
                }
                if (args[0].equalsIgnoreCase("?")) {
                    sender.sendMessage(ChatColor.YELLOW + "Use "
                            + ChatColor.GOLD + "'/serubans -option' "
                            + ChatColor.YELLOW + "for debug functionality.");
                    sender.sendMessage(ChatColor.YELLOW + "Options:");
                    sender.sendMessage(ChatColor.YELLOW
                            + "-a    prints full hashmaps lists");
                    sender.sendMessage(ChatColor.YELLOW
                            + "-p    prints player hashmaps lists");
                    sender.sendMessage(ChatColor.YELLOW
                            + "-i     prints id  hashmaps lists");
                    sender.sendMessage(ChatColor.YELLOW
                            + "-b    prints banned player hashmaps lists");
                    sender.sendMessage(ChatColor.YELLOW
                            + "-w    prints warns hashmaps lists");
                    sender.sendMessage(ChatColor.YELLOW
                            + "-e    export bans to minecraft bans files");
                    return true;
                }
                if (args[0].startsWith("-")) {
                    if (args[0].contains("a") && !args[0].contains("api")) {
                        sender.sendMessage("Players: "
                                + DatabaseCache.getFullPlayerList());
                        sender.sendMessage("Banned Players: "
                                + DatabaseCache.getFullBannedPlayers());
                        sender.sendMessage("TempBan: "
                                + DatabaseCache.getFullTempBannedTime());
                        sender.sendMessage("Ids: " + DatabaseCache.getFullIds());
                        return true;
                    }
                    if (args[0].contains("p") && !args[0].contains("api")) {
                        sender.sendMessage("Players: "
                                + DatabaseCache.getFullPlayerList());
                    }
                    if (args[0].contains("i") && !args[0].contains("api")) {
                        sender.sendMessage("Ids: " + DatabaseCache.getFullIds());
                    }
                    if (args[0].contains("b")) {
                        sender.sendMessage("Banned Players: "
                                + DatabaseCache.getFullBannedPlayers());
                    }
                    if (args[0].contains("t")) {
                        sender.sendMessage("TempBan: "
                                + DatabaseCache.getFullTempBannedTime());
                    }
                    if (args[0].contains("w")) {
                        sender.sendMessage("Warns: "
                                + DatabaseCache.getFullWarnList());
                    }
                    if (args[0].contains("e")) {
                        List<String> ban = DatabaseCache.getBannedForFile();
                        Iterator<String> iterator = ban.iterator();
                        try {
                            BufferedWriter banlist = new BufferedWriter(
                                    new FileWriter("banned-players.txt", true));

                            while (iterator.hasNext()) {
                                String player = iterator.next();
                                banlist.write(player);
                                banlist.newLine();
                            }
                            banlist.close();
                        } catch (IOException e) {
                            plugin.log.severe("File Could not be writen!");
                        }
                    }
                    return true;
                }
                return false;
            } else {
                sender.sendMessage(ChatColor.RED
                        + "You do not have permission!");
                return true;
            }
        }
        return false;
    }
}
