package net.serubin.serubans;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.serubin.serubans.util.ArgProcessing;
import net.serubin.serubans.util.DatabaseCache;

public class SeruBansDatabase implements Runnable {

	private static Connection conn;
	private static String host;
	private static String username;
	private static String password;
	private static String database;
	private static String reason;
	private static String mod;
	private static int lastBanId;
	private static int lastUserId;
	private static SeruBans plugin;

	/**
	 * Initiates Mysql object
	 * 
	 * @param host
	 *            of the mysql server
	 * @param username
	 *            of the mysql account
	 * @param password
	 *            of the mysql account
	 * @param database
	 *            to be used
	 * @param plugin
	 */
	public SeruBansDatabase(String host, String username, String password,
			String database, SeruBans plugin) {
		SeruBansDatabase.host = host;
		SeruBansDatabase.username = username;
		SeruBansDatabase.password = password;
		SeruBansDatabase.database = database;
		SeruBansDatabase.plugin = plugin;
	}

	/**
	 * Start sql connections
	 */
	public static void startSQL() {
		createConnection();
		createTable();
		getPlayer();
		getBans();
		getTempBans();
		getBanIds();
		getWarns();
	}

	/**
	 * Update sql cache
	 */
	public static void updateCACHE() {
		getNewPlayer();
		getNewBans();
		getNewTempBans();
		getNewBanIds();
		getNewWarns();
	}

	/**
	 * Create MySQL connection
	 */
	protected static void createConnection() {
		String sqlUrl = String.format("jdbc:mysql://%s/%s", host, database);

		Properties sqlStr = new Properties();
		sqlStr.put("user", username);
		sqlStr.put("password", password);
		try {
			setConn(DriverManager.getConnection(sqlUrl, sqlStr));
		} catch (SQLException e) {
			SeruBans.printError("A MySQL connection could not be made");
			e.printStackTrace();
		}
	}

	/**
	 * Checks if tables are existing, then
	 */
	protected static void createTable() {
		try {
			SeruBans.printInfo("Searching for storage table");
			ResultSet rs = getConn().getMetaData().getTables(null, null,
					"bans", null);
			if (!rs.next()) {
				SeruBans.printWarning("No 'bans' data table found, Attempting to create one...");
				PreparedStatement ps = getConn()
						.prepareStatement(
								"CREATE TABLE IF NOT EXISTS `bans` ( "
										+ "`id` mediumint unsigned not null auto_increment, "
										+ "`player_id` mediumint unsigned not null, "
										+ "`type` tinyint(2) not null, "
										+ "`length` bigint(20) not null, "
										+ "`mod` mediumint(8) unsigned not null, "
										+ "`date` DATETIME not null, "
										+ "`reason` varchar(255) not null, "
										+ "`display` tinyint(1) not null, "
										+ "primary key (`id`));");
				ps.executeUpdate();
				ps.close();
				SeruBans.printWarning("'bans' data table created!");
			} else {
				SeruBans.printInfo("Table found");
			}
			rs.close();

			SeruBans.printInfo("Searching for log table");
			rs = getConn().getMetaData().getTables(null, null, "log", null);
			if (!rs.next()) {
				SeruBans.printWarning("No 'log' data table found, Attempting to create one...");
				PreparedStatement ps = getConn()
						.prepareStatement(
								"CREATE TABLE IF NOT EXISTS `log` ( "
										+ "`id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT, "
										+ "`action` enum('delete','unban','update') NOT NULL, "
										+ "`banid` mediumint(8) unsigned NOT NULL, "
										+ "`ip` text NOT NULL, "
										+ "`data` text NOT NULL, "
										+ "primary key (`id`), key `id` (`id`));");
				ps.executeUpdate();
				ps.close();
				SeruBans.printWarning("'log' data table created!");
			} else {
				SeruBans.printInfo("Table found");
			}
			rs.close();

			SeruBans.printInfo("Searching for warns table");
			rs = getConn().getMetaData().getTables(null, null, "warns", null);
			if (!rs.next()) {
				SeruBans.printWarning("No 'warns' data table found, Attempting to create one...");
				PreparedStatement ps = getConn()
						.prepareStatement(
								"CREATE TABLE IF NOT EXISTS `warns` ( "
										+ "`id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT, "
										+ "`player_id` mediumint(8) unsigned NOT NULL, "
										+ "`ban_id` mediumint(8) unsigned NOT NULL, "
										+ "primary key (`id`), key `id` (`id`));");
				ps.executeUpdate();
				ps.close();
				SeruBans.printWarning("'warns' data table created!");
			} else {
				SeruBans.printInfo("Table found");
			}
			rs.close();

			SeruBans.printInfo("Searching for users table");
			rs = getConn().getMetaData().getTables(null, null, "users", null);
			if (!rs.next()) {
				SeruBans.printWarning("No 'users' data table found, Attempting to create one...");
				PreparedStatement ps = getConn()
						.prepareStatement(
								"CREATE TABLE IF NOT EXISTS `users` ( "
										+ "`id` mediumint unsigned not null auto_increment, "
										+ "`name` varchar(16) not null, "
										+ "primary key (`id`), UNIQUE key `player` (`name`));");
				ps.executeUpdate();
				ps.close();
				SeruBans.printWarning("'users' data table created!");
			} else {
				SeruBans.printInfo("Table found");
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void getPlayer() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement("SELECT * FROM users;");
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer pId = rs.getInt("id");
				String pName = rs.getString("name");
				DatabaseCache.setPlayerList(pName, pId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void getBans() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT bans.player_id, bans.id, users.name, users.id"
							+ " FROM bans" + " INNER JOIN users"
							+ "  ON bans.player_id=users.id"
							+ " WHERE (type = 1 OR type = 2) ");
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer bId = rs.getInt("bans.id");
				String pName = rs.getString("name");
				DatabaseCache.setBannedPlayers(pName, bId);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public static void getBanIds() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement("SELECT bans.id FROM bans;");
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer id = rs.getInt("bans.id");
				DatabaseCache.setIds(id);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public static void getTempBans() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT id, length" + " FROM bans" + " WHERE (type = 2) ");
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer bId = rs.getInt("id");
				Long length = rs.getLong("length");
				DatabaseCache.setTempBannedTime(bId, length);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

	}

	public static void getWarns() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT player_id, ban_id FROM warns;");
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer pId = rs.getInt("player_id");
				Integer bId = rs.getInt("ban_id");
				List<Integer> warns;
				if (DatabaseCache.isWarn(pId)) {
					warns = DatabaseCache.getWarn(pId);
					warns.add(bId);
				} else {
					warns = new ArrayList<Integer>();
					warns.add(bId);
				}
				DatabaseCache.setWarn(pId, warns);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public static void getNewPlayer() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT * FROM users WHERE (users.id > ?);");
			ps.setInt(1, getLastUserId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer pId = rs.getInt("id");
				String pName = rs.getString("name");
				DatabaseCache.setPlayerList(pName, pId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void getNewBans() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn()
					.prepareStatement(
							"SELECT bans.player_id, bans.id, users.name, users.id"
									+ " FROM bans"
									+ " INNER JOIN users"
									+ "  ON bans.player_id=users.id"
									+ " WHERE (type = 1 OR type = 2) AND (bans.id > ?)");
			ps.setInt(1, getLastBanId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer bId = rs.getInt("bans.id");
				String pName = rs.getString("name");

				Player illegal = plugin.getServer().getPlayer(pName);

				if (illegal != null) {
					illegal.kickPlayer(ArgProcessing.GetColor(ArgProcessing
							.PlayerMessage("Automatic ban enforcement",
									"Automatic ban enforcement", "<Ban-system>")));
				}
				DatabaseCache.setBannedPlayers(pName, bId);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public static void getNewBanIds() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT bans.id FROM bans WHERE (bans.id > ?);");
			ps.setInt(1, getLastBanId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer id = rs.getInt("bans.id");
				DatabaseCache.setIds(id);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public static void getNewTempBans() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT bans.id, bans.length" + " FROM bans "
							+ " WHERE (bans.type = 2) AND (bans.id > ?)");
			ps.setInt(1, getLastBanId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer bId = rs.getInt("id");
				Long length = rs.getLong("length");
				DatabaseCache.setTempBannedTime(bId, length);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

	}

	public static void getNewWarns() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT player_id, ban_id FROM warns WHERE (ban_id > ?);");
			ps.setInt(1, getLastBanId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer pId = rs.getInt("player_id");
				Integer bId = rs.getInt("ban_id");
				List<Integer> warns;
				if (DatabaseCache.isWarn(pId)) {
					warns = DatabaseCache.getWarn(pId);
					warns.add(bId);
				} else {
					warns = new ArrayList<Integer>();
					warns.add(bId);
				}
				DatabaseCache.setWarn(pId, warns);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public static void addBan(String victim, int type, long length, String mod,
			String reason, int display) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		checkForNewBans();
		// add player
		try {
			ps = getConn()
					.prepareStatement(
							"INSERT INTO bans (`player_id`, `type`, `length`, `mod`, `date`, `reason`, `display`) VALUES(?,?,?,?,?,?,?);",
							Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, DatabaseCache.getPlayerList(victim.toLowerCase()));
			ps.setInt(2, type);
			ps.setLong(3, length);
			ps.setInt(4, DatabaseCache.getPlayerList(mod.toLowerCase()));
			ps.setObject(5, ArgProcessing.getDateTime());
			ps.setString(6, reason);
			ps.setInt(7, display);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				Integer bId = rs.getInt(1);
				DatabaseCache.setIds(bId);
				if (type == 1 || type == 2) {
					DatabaseCache.setBannedPlayers(victim.toLowerCase(), bId);
				}
				if (type == 2) {
					DatabaseCache.setTempBannedTime(bId, length);
				}
				setLastBanId(bId);
				SeruBans.printInfo(ArgProcessing.getBanTypeString(type) + ": "
						+ victim + " Ban Id: " + bId);
			} else {
				SeruBans.printInfo("Error adding ban!");
			}
		} catch (SQLException e) {
			SeruBans.printInfo("Error adding ban!");
			e.printStackTrace();
		}
	}

	public static void updateBan(int type, int bId) {
		PreparedStatement ps = null;
		try {
			ps = getConn().prepareStatement(
					"UPDATE bans SET type=? WHERE id=?;");
			ps.setInt(1, type);
			ps.setInt(2, bId);
			ps.executeUpdate();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public static void updateReason(int bId, String reason) {
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		try {
			ps = getConn().prepareStatement(
					"UPDATE bans SET reason=? WHERE id=?;");
			ps2 = getConn()
					.prepareStatement(
							"INSERT INTO `log`(`action`, `banid`, `ip`, `data`) VALUES ('update',?,'In-Game',?)");
			String data = "UPDATE:Id=" + bId + "Reason=" + reason;
			ps2.setInt(1, bId);
			ps2.setString(2, data);
			ps2.executeUpdate();
			ps.setString(1, reason);
			ps.setInt(2, bId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean findPlayer(String victim) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		SeruBans.printInfo("Attempting to add player " + victim
				+ " to database;");
		// add player
		try {
			ps = getConn().prepareStatement(
					"SELECT * FROM users WHERE name = ?;");
			ps.setString(1, victim.toLowerCase());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer pId = rs.getInt("id");
				String pName = rs.getString("name");
				DatabaseCache.setPlayerList(pName, pId);
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int addOrFindPlayer(String player) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		SeruBans.printInfo("Attempting to add player " + player
				+ " to database;");
		checkForNewBans();
		Integer key = DatabaseCache.getBannedPlayers(player);
		if (key == null) {
			try {
				// add player
				ps = getConn().prepareStatement(
						"INSERT INTO users (name) VALUES(?);",
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, player);
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					Integer pId = rs.getInt(1);
					setLastUserId(pId);
					DatabaseCache.setPlayerList(player, pId);
					SeruBans.printInfo("Player Added: " + player + " Id: "
							+ pId);
					return pId;
				} else {
					SeruBans.printInfo("Error adding user!");
					return -1;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return -1;
			}
		} else {
			return key;
		}
	}

	public static String getReason(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		reason = "";
		try {
			ps = getConn().prepareStatement(
					"SELECT id, reason FROM bans WHERE (id = ?);");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				reason = rs.getString("reason");
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return reason;
	}

	public static String getMod(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		mod = "";
		try {
			ps = getConn()
					.prepareStatement(
							"SELECT bans.id, bans.mod, users.id, users.name FROM bans INNER JOIN users ON bans.mod = users.id WHERE (bans.id = ?);");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				mod = rs.getString("name");
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return mod;
	}

	public static Long getLength(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Long length = null;
		try {
			ps = getConn().prepareStatement(
					"SELECT id, length FROM bans WHERE (id = ?);");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				length = rs.getLong("length");
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return length;
	}

	public static void addWarn(int pId, int bId) {
		PreparedStatement ps = null;
		try {
			ps = getConn().prepareStatement(
					"INSERT INTO warns (player_id, ban_id) VALUES(?,?);",
					Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, pId);
			ps.setInt(2, bId);
			ps.executeUpdate();
			ps.getGeneratedKeys();
			List<Integer> warns;
			if (DatabaseCache.isWarn(pId)) {
				warns = DatabaseCache.getWarn(pId);
				warns.add(bId);
			} else {
				warns = new ArrayList<Integer>();
				warns.add(bId);
			}
			DatabaseCache.setWarn(pId, warns);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void removeWarn(int pId, int bId) {
		PreparedStatement ps = null;
		try {
			ps = getConn().prepareStatement(
					"DELETE FROM warns WHERE player_id=? AND ban_id=?;");
			ps.setInt(1, pId);
			ps.setInt(2, bId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<Integer> searchPlayer(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Integer> type = new ArrayList<Integer>();
		try {
			ps = getConn()
					.prepareStatement(
							"SELECT `player_id`, `type` FROM bans WHERE (player_id = ?);");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				type.add(rs.getInt("type"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (type.isEmpty()) {
			type = null;
		}
		return type;
	}

	public static List<String> searchType(int id, int type) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Integer> typeList = new ArrayList<Integer>();
		List<String> PlayerInfo = new ArrayList<String>();
		try {
			if ((type == 1) || (type == 2)) {
				int type2 = 0;
				if (type == 1) {
					type2 = 11;
				} else if (type == 2) {
					type2 = 12;
				}
				ps = getConn()
						.prepareStatement(
								"SELECT `player_id`, `type`, `id` FROM bans WHERE ((player_id = ?) AND (type = ?)) OR ((player_id = ?) AND (type = ?));");

				ps.setInt(1, id);
				ps.setInt(2, type);
				ps.setInt(3, id);
				ps.setInt(4, type2);
			} else {
				ps = getConn()
						.prepareStatement(
								"SELECT `player_id`, `type`, `id` FROM bans WHERE (player_id = ?) AND (type = ?);");

				ps.setInt(1, id);
				ps.setInt(2, type);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				typeList.add(rs.getInt("id"));
			}

			if (typeList.isEmpty()) {
				return PlayerInfo = null;
			}

			Iterator<Integer> typeListItor = typeList.iterator();
			while (typeListItor.hasNext()) {
				PlayerInfo.add(getPlayerInfo(typeListItor.next()));
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return PlayerInfo;
	}

	public static String getPlayerInfo(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String line = null;
		new ArrayList<String>();
		try {
			ps = getConn()
					.prepareStatement(
							"SELECT bans.id, bans.mod, users.id, users.name, bans.type, bans.reason, bans.length, bans.date FROM bans INNER JOIN users ON bans.mod = users.id WHERE (bans.id = ?);");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				int bId = rs.getInt("bans.id");
				int tId = rs.getInt("bans.type");
				String mName = rs.getString("users.name");
				String date = rs.getObject("bans.date").toString();
				String reason = rs.getString("bans.reason");
				if (tId == 2) {
					rs.getLong("bans.length");
				}
				line = bId + " - " + ArgProcessing.getBanTypeString(tId)
						+ " - " + mName + " - " + date + " - " + reason;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return line;
	}

	/**
	 * Get ban id info
	 * 
	 * @param id
	 *            ban id
	 * @return
	 */
	public static Map<String, String> getBanIdInfo(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, String> BanId = new HashMap<String, String>();
		int pId = 0;
		try {
			ps = getConn()
					.prepareStatement(
							"SELECT bans.player_id, bans.id, bans.mod, users.id, users.name, bans.type, bans.reason, bans.length, bans.date FROM bans INNER JOIN users ON bans.mod = users.id WHERE (bans.id = ?);");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				pId = rs.getInt("bans.player_id");
				int tId = rs.getInt("bans.type");
				String mName = rs.getString("users.name");
				String date = rs.getObject("bans.date").toString();
				String reason = rs.getString("bans.reason");
				Long length = null;
				if (tId == 2) {
					length = rs.getLong("bans.length");
					BanId.put("length", Long.toString(length));
				}
				BanId.put("type", ArgProcessing.getBanTypeString(tId));
				BanId.put("mod", mName);
				BanId.put("date", date);
				BanId.put("reason", reason);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			ps = getConn().prepareStatement(
					"SELECT `name` FROM users WHERE (`id` = ?);");
			ps.setInt(1, pId);
			rs = ps.executeQuery();

			if (rs.next()) {
				String name = rs.getString("name");
				BanId.put("name", name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return BanId;
	}

	/**
	 * Gets last ban id.
	 * 
	 * @return lastBanId
	 */
	public static int getLastBanId() {
		return lastBanId;
	}

	/**
	 * Gets last ban user id.
	 * 
	 * @return lastUserId
	 */
	private static int getLastUserId() {
		return lastUserId;
	}

	private static void setLastBanId(int lastBanId) {
		SeruBansDatabase.lastBanId = lastBanId;
	}

	private static void setLastUserId(int lastUserId) {
		SeruBansDatabase.lastUserId = lastUserId;
	}

	public static Connection getConn() {
		return conn;
	}

	private static void setConn(Connection conn) {
		SeruBansDatabase.conn = conn;
	}

	public static void checkForNewBans() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean update = false;
		try {
			ps = getConn().prepareStatement(
					"SELECT max(users.id), max(bans.id) FROM bans, users;");
			rs = ps.executeQuery();
			if (rs.next()) {
				update = rs.getInt(1) != getLastBanId();
				update = update || rs.getInt(1) != getLastUserId();

				if (update) {
					SeruBans.self.log
							.fine("SeruBans has checked in and updating cache from database");
					updateCACHE();
					setLastBanId(rs.getInt(1));
					setLastUserId(rs.getInt(1));
				} else {
					SeruBans.self.log
							.fine("SeruBans has checked in with database cache");
				}
			}
		} catch (SQLException e) {
			SeruBans.self.log.log(Level.WARNING,
					"SeruBans has encoutered a SQLEception: ", e);
		}
	}

	public static void checkForUnBans() {
		if (!DatabaseCache.getBanIds().isEmpty()) {
			String sql = "";
			for (Integer id : DatabaseCache.getBanIds())
				sql += " " + id + ",";
			sql = sql.substring(0, sql.lastIndexOf(","));

			try {
				PreparedStatement ps;
				ps = getConn()
						.prepareStatement(
								"SELECT bans.id, users.name"
										+ " FROM bans"
										+ " INNER JOIN users"
										+ "  ON bans.player_id=users.id"
										+ " WHERE (type = 11 OR type = 12) AND bans.id IN ("
										+ sql + " )");
				ResultSet rs = ps.getResultSet();

				int bId;
				String BannedVictim;
				while (rs.next()) {
					bId = rs.getInt("id");
					BannedVictim = rs.getString("name");
					if (DatabaseCache.keyIsInTempBannedTime(bId)) {
						DatabaseCache.removeTempBannedTimeItem(bId);
					}
					DatabaseCache.removeBannedPlayerItem(BannedVictim
							.toLowerCase());
					SeruBans.printServer(ChatColor.YELLOW + BannedVictim
							+ ChatColor.GOLD
							+ " was automaticly unbanned. From another server",
							false);
					plugin.log.info(BannedVictim
							+ " was unbanned by <ban-system>");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void checkForExpiredTempBans() {
		int b_Id;
		List<String> toUnban = DatabaseCache.getTempBannedTimeUnbans();
		Iterator<String> iterator = toUnban.iterator();
		while (iterator.hasNext()) {
			String player = iterator.next();
			b_Id = DatabaseCache.getBannedPlayers(player);
			DatabaseCache.removeBannedPlayerItem(player);
			DatabaseCache.removeTempBannedTimeItem(b_Id);
			SeruBansDatabase.updateBan(SeruBans.UNTEMPBAN, b_Id);
			plugin.printDebug(player
					+ "has been unbanned by per minute tempban checker");
		}
	}

	public void run() {
		plugin.printDebug("Check update bans thread has started.");
		plugin.printDebug(Long.toString(System.currentTimeMillis() / 1000));

		checkForNewBans();
		checkForUnBans();
		checkForExpiredTempBans();

		plugin.printDebug("Check update bans thread has stopped");
	}
}
