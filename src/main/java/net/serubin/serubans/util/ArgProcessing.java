package net.serubin.serubans.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import net.serubin.serubans.SeruBans;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class ArgProcessing {

	static OfflinePlayer offPlayer;

	public static Player getVictim(String[] args) {
		Server server = Bukkit.getServer();
		Player victim = server.getPlayer(args[0]);

		if (victim != null) {
			// if victim is on return victim
			return victim;
		} else {
			// if not check offline players (try catch)
			try {
				victim = offPlayer.getPlayer();
			} catch (NullPointerException NPE) {
				// if not found victim is null
				victim = null;

			}
			return victim;
		}
	}

	public static String reasonArgs(String[] args) {
		StringBuilder reasonRaw = new StringBuilder();
		String reason;
		// combine args into a string
		for (String s : args) {
			reasonRaw.append(" " + s);
		}
		reason = reasonRaw.toString().replaceFirst(" " + args[0] + " ", "");
		// return string
		return reason;
	}

	public static String reasonOptArgs(String[] args) {
		StringBuilder reasonRaw = new StringBuilder();
		String reason;
		// combine args into a string
		for (String s : args) {
			reasonRaw.append(" " + s);
		}
		reason = reasonRaw.toString().replaceFirst(
				" " + args[0] + " " + args[1] + " ", "");
		// return string
		return reason;
	}

	public static String reasonArgsTB(String[] args) {
		StringBuilder reasonRaw = new StringBuilder();
		String reason;
		// combine args into a string
		for (String s : args) {
			reasonRaw.append(" " + s);
		}
		reason = reasonRaw.toString().replaceFirst(
				" " + args[0] + " " + args[1] + " " + args[2] + " ", "");
		// return string
		return reason;
	}

	public static String GetColor(String line) {
		line = line.replace("&0", "§0");
		line = line.replace("&1", "§1");
		line = line.replace("&2", "§2");
		line = line.replace("&3", "§3");
		line = line.replace("&4", "§4");
		line = line.replace("&5", "§5");
		line = line.replace("&6", "§6");
		line = line.replace("&7", "§7");
		line = line.replace("&8", "§8");
		line = line.replace("&9", "§9");
		line = line.replace("&a", "§a");
		line = line.replace("&b", "§b");
		line = line.replace("&c", "§c");
		line = line.replace("&d", "§d");
		line = line.replace("&e", "§e");
		line = line.replace("&f", "§f");
		return line;
	}

	public static String GlobalMessage(String line, String reason, String mod,
			String victim) {
		line = line.replaceAll("%victim%", victim);
		line = line.replaceAll("%reason%", reason);
		line = line.replaceAll("%kicker%", mod);
		return line;
	}

	public static String PlayerMessage(String line, String reason, String mod) {

		line = line.replaceAll("%reason%", reason);
		line = line.replaceAll("%kicker%", mod);
		return line;
	}

	public static String GlobalTempBanMessage(String line, String reason,
			String mod, String victim, String time) {
		line = line.replaceAll("%victim%", victim);
		line = line.replaceAll("%reason%", reason);
		line = line.replaceAll("%kicker%", mod);
		line = line.replaceAll("%time%", time);
		return line;
	}

	public static String PlayerTempBanMessage(String line, String reason,
			String mod, String time) {

		line = line.replaceAll("%reason%", reason);
		line = line.replaceAll("%kicker%", mod);
		line = line.replaceAll("%time%", time);
		return line;
	}

	public static long parseTimeSpec(String time, String unit) {
		long sec;
		try {
			sec = Integer.parseInt(time) * 60 * 60;
		} catch (NumberFormatException ex) {
			return 0;
		}
		
		if (unit.startsWith("s"))
			sec /= (60 * 60);
		else if (unit.startsWith("mi"))
			sec /= 60;
//		else if (unit.startsWith("h"))
//			sec *= 1;
		else if (unit.startsWith("d"))
			sec *= 24;
		else if (unit.startsWith("w"))
			sec *= (7 * 24);
		else if (unit.startsWith("m"))
			sec *= (30 * 24);
		
		return sec;
	}

	public static Timestamp getDateTime() {
		java.sql.Timestamp date;
		java.util.Date today = new java.util.Date();
		date = new java.sql.Timestamp(today.getTime());
		return date;
	}

	public static String getStringDate(long length) {
		Date date = new Date();
		date.setTime(length * 1000);
		String dates = date.toString();

		return dates;
	}

	public static long getUnixTimeStamp(Timestamp timestamp) {
		long unixTS = 0;
		try {
			unixTS = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.parse(timestamp.toString()).getTime();
		} catch (ParseException e) {
			SeruBans.self.log
					.severe("Could not convert SQL Timestamp to unix timestamp, This means tempbans probably did not get loaded properly.");

			e.printStackTrace();
		}
		return unixTS;
	}

	public static String getBanTypeString(int Type) {
		switch (Type) {
		case SeruBans.BAN:
			return "Ban";
		case SeruBans.TEMPBAN:
			return "TempBan";
		case SeruBans.WARN:
			return "Warning";
		case SeruBans.KICK:
			return "Kick";
		case SeruBans.UNBAN:
			return "Unban";
		case SeruBans.UNTEMPBAN:
			return "Untempban";
		default:
			return "";
		}
	}

	public static String[] stripFirstArg(String[] args) {
		String[] argNew = new String[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			argNew[i - 1] = args[i];
		}
		return argNew;
	}

	public static String[] stripTimeArg(String[] args) {
		String[] argNew = new String[args.length - 2];
		argNew[0] = args[0];
		for (int i = 3; i < args.length; i++) {
			argNew[i - 2] = args[i];
		}
		return argNew;
	}

}