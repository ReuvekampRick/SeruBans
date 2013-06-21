package net.serubin.serubans.commands;

import net.serubin.serubans.SeruBans;
import net.serubin.serubans.util.DatabaseCache;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CheckBanCommand implements CommandExecutor {

    public CheckBanCommand(SeruBans plugin) {
    }

    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {

        if (commandLabel.equalsIgnoreCase("checkban")) {
            if (SeruBans.hasPermission(sender, SeruBans.CHECKBANPERM)) {
                if (args.length != 1) {
                    return false;
                }
                boolean isBanned = DatabaseCache.keyIsInBannedPlayers(args[0]);

                if (isBanned) {
                    int id = DatabaseCache.getBannedPlayers(args[0]);
                    sender.sendMessage(ChatColor.RED + args[0] + " is banned.");
                    sender.sendMessage(ChatColor.RED + "Ban id: "
                            + ChatColor.YELLOW + id);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.GREEN + args[0]
                            + " is not banned.");
                    return true;
                }
            }
            return true;
        }
        return false;
    }

}
