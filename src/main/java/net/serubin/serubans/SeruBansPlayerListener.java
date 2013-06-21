package net.serubin.serubans;

import java.util.List;

import net.serubin.serubans.util.ArgProcessing;
import net.serubin.serubans.util.DatabaseCache;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class SeruBansPlayerListener implements Listener {

    private SeruBans plugin;
    private String banMessage;
    public boolean tempban = false;
    private String tempBanMessage;

    public SeruBansPlayerListener(SeruBans plugin, String banMessage,
            String tempBanMessage) {
        this.plugin = plugin;
        this.banMessage = banMessage;
        this.tempBanMessage = tempBanMessage;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        tempban = false;
        Player player = event.getPlayer();
        plugin.printDebug(player.getName() + " is attempting to login");
        // checks if player is banned
        if (DatabaseCache.keyIsInBannedPlayers(player.getName().toLowerCase())) {
            int bId = DatabaseCache.getBannedPlayers(player.getName().toLowerCase());
            if (DatabaseCache.keyIsInTempBannedTime(bId)) {
                plugin.printDebug(player.getName() + "Is tempbaned");
                tempban = true;
                if (DatabaseCache.getTempBannedTime(bId) < System
                        .currentTimeMillis() / 1000) {
                    DatabaseCache.removeBannedPlayerItem(player.getName()
                            .toLowerCase());
                    DatabaseCache.removeTempBannedTimeItem(bId);
                    SeruBansDatabase.updateBan(SeruBans.UNTEMPBAN, bId);
                    return;
                } else {

                }
            }
            plugin.log.warning(player.getName() + " LOGIN DENIED - BANNED");
            int b_Id = DatabaseCache
                    .getBannedPlayers(player.getName().toLowerCase());
            String reason = SeruBansDatabase.getReason(b_Id);
            String mod = SeruBansDatabase.getMod(b_Id);
            // Handles tempban stuff
            if (tempban) {
                plugin.printDebug(player.getName() + "tempban");
                Long length = SeruBansDatabase.getLength(b_Id);
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                        ArgProcessing.GetColor(ArgProcessing
                                .PlayerTempBanMessage(tempBanMessage, reason,
                                        mod,
                                        ArgProcessing.getStringDate(length))));
            } else {
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                        ArgProcessing.GetColor(ArgProcessing.PlayerMessage(
                                banMessage, reason, mod)));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        int pId = 0;
        if (!DatabaseCache.keyIsInPlayerList(player.getName().toLowerCase())) {
            return;
        }
        pId = DatabaseCache.getPlayerList(player.getName().toLowerCase());
        if (DatabaseCache.isWarn(pId)) {
            final List<Integer> bId = DatabaseCache.getWarn(pId);
            for (int i : bId) {
                SeruBans.printInfo("Warning player, ban id:"
                        + Integer.toString(i));
                final String message = ArgProcessing.GetColor(ArgProcessing
                        .PlayerMessage(SeruBans.WarnPlayerMessage,
                                SeruBansDatabase.getReason(i),
                                SeruBansDatabase.getMod(i)));
                plugin.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, new Runnable() {
                            public void run() {
                                player.sendMessage(message);
                            }
                        });
                SeruBansDatabase.removeWarn(pId, i);
                DatabaseCache.remWarn(pId);
            }
        }

    }
}
