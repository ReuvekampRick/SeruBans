package net.serubin.serubans.commands;

import net.serubin.serubans.SeruBansDatabase;
import net.serubin.serubans.SeruBans;
import net.serubin.serubans.util.ArgProcessing;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {

    private String KickMessage;
    private String GlobalKickMessage;
    private SeruBans plugin;

    public KickCommand(String KickMessage, String GlobalKickMessage,
            String name, SeruBans plugin) {
        this.KickMessage = KickMessage;
        this.GlobalKickMessage = GlobalKickMessage;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        Player victim;
        String mod;
        String reason;
        int display = SeruBans.SHOW;
        boolean silent = false;

        if (commandLabel.equalsIgnoreCase("kick")) {
            if (SeruBans.hasPermission(sender, SeruBans.KICKPERM)) {

                // checks for options
                // TODO Make this more efficient
                if (args.length == 0
                        || (args.length == 1 && args[0].startsWith("-"))) {
                    return false;
                } else {
                    silent = false;
                    display = SeruBans.SHOW;
                    if (args[0].startsWith("-")) {
                        if (args[0].contains("s")) {
                            silent = true;
                        }
                        if (args[0].contains("h")) {
                            display = SeruBans.HIDE;
                        }
                        args = ArgProcessing.stripFirstArg(args);
                    }
                }

                if (args.length == 0) {
                    return false;
                } else if (args.length > 1) {
                    reason = ArgProcessing.reasonArgs(args);
                } else {
                    reason = "undefined";
                }
                mod = sender.getName();
                victim = plugin.getServer().getPlayer(args[0]);
                if (victim != null) {
                    // checks players for id in database
					SeruBansDatabase.addOrFindPlayer(args[0].toLowerCase());
					SeruBansDatabase.addOrFindPlayer(sender.getName().toLowerCase());
                    // adds ban to database
                    SeruBansDatabase.addBan(victim.getName(), SeruBans.KICK, 0,
                            mod, reason, display);
                    // prints to players on server with perms
                    SeruBans.printServer(ArgProcessing.GlobalMessage(
                            GlobalKickMessage, reason, mod, victim.getName()),
                            silent);
                    // logs its
                    plugin.log.info(mod + " kicked " + victim.getName()
                            + " for " + reason);
                    // sends kicker ban id
                    sender.sendMessage(ChatColor.GOLD + "Ban Id: "
                            + ChatColor.YELLOW
                            + Integer.toString(SeruBansDatabase.getLastBanId()));
                    // kicks player of the server
                    victim.kickPlayer(ArgProcessing.GetColor(ArgProcessing
                            .PlayerMessage(KickMessage, reason, mod)));
                    // adds player to db
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED
                            + "This Player was not found!");
                    return true;
                }
            }
            return true;
        }

        return false;
    }

}
