package net.serubin.serubans.commands;

import net.serubin.serubans.SeruBansDatabase;
import net.serubin.serubans.SeruBans;
import net.serubin.serubans.util.ArgProcessing;
import net.serubin.serubans.util.DatabaseCache;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanCommand implements CommandExecutor {

	private String BanMessage;
	private String GlobalBanMessage;
	private SeruBans plugin;
	private KickCommand kickCommand;

	public BanCommand(String BanMessage, String GlobalBanMessage, String name,
			KickCommand kickCommand, SeruBans plugin) {
		this.BanMessage = BanMessage;
		this.GlobalBanMessage = GlobalBanMessage;
		this.plugin = plugin;
		this.kickCommand = kickCommand;
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player victim;
		String mod;
		String reason;
		boolean silent = false;
		int display = SeruBans.SHOW;
		if (commandLabel.equalsIgnoreCase("ban")) {
			if (SeruBans.hasPermission(sender, SeruBans.BANPERM)) {

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

				SeruBansDatabase.addOrFindPlayer(args[0]);
				SeruBansDatabase.addOrFindPlayer(sender.getName());
				if (victim != null) {
					// checks if banned
					if (!DatabaseCache.keyIsInBannedPlayers(victim.getName())) {
						// adds ban to database
						SeruBansDatabase.addBan(victim.getName(), SeruBans.BAN, 0, mod,
								reason, display);

						// prints to players on server with perms
						SeruBans.printServer(
								ArgProcessing.GlobalMessage(GlobalBanMessage,
										reason, mod, victim.getName()), silent);
						// logs it
						plugin.log.info(mod + " banned " + victim.getName()
								+ " for " + reason);
						// sends kicker ban id
						sender.sendMessage(ChatColor.GOLD + "Ban Id: "
								+ ChatColor.YELLOW
								+ Integer.toString(SeruBansDatabase.getLastBanId()));
						// kicks player
						victim.kickPlayer(ArgProcessing.GetColor(ArgProcessing
								.PlayerMessage(BanMessage, reason, mod)));
						return true;
					} else {
						sender.sendMessage(ChatColor.GOLD
								+ victim.getName()
								+ ChatColor.RED
								+ " is already banned! You ban has been altered to a kick");
						kickCommand.onCommand(sender, cmd, commandLabel, args);
						return true;
					}
				} else {
					// checks if banned
					if (!DatabaseCache.keyIsInBannedPlayers(args[0])) {
						// adds ban to database
						SeruBansDatabase.addBan(args[0], SeruBans.BAN, 0, mod, reason,
								display);
						// prints to players on server with perms
						SeruBans.printServer(ArgProcessing.GlobalMessage(
								GlobalBanMessage, reason, mod, args[0]), silent);
						// logs it
						plugin.log.info(mod + " banned " + args[0] + " for "
								+ reason);
						// sends kicker ban id
						sender.sendMessage(ChatColor.GOLD + "Ban Id: "
								+ ChatColor.YELLOW
								+ Integer.toString(SeruBansDatabase.getLastBanId()));
						return true;
					} else {
						sender.sendMessage(ChatColor.GOLD + args[0]
								+ ChatColor.RED + " is already banned!");
						return true;
					}

				}
			}
			return true;
		}
		return false;
	}
}
