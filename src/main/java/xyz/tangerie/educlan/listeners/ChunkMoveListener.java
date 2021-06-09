package xyz.tangerie.educlan.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import xyz.tangerie.educlan.managers.ClansManager;
import xyz.tangerie.educlan.models.ECClan;

public class ChunkMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        ECClan fromClan = ClansManager.getInstance().getClanByChunk(e.getFrom().getChunk());
        ECClan toClan = ClansManager.getInstance().getClanByChunk(e.getTo().getChunk());

        //Moving from wilderness to wilderness
        if(fromClan == null && toClan == null) {
            return;
        }

        //Travelling to wilderness
        if(toClan == null) {
            e.getPlayer().sendTitle(ChatColor.GREEN + "Wilderness", ChatColor.GREEN + "Entering No Man's Land", 5, 50, 5);
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(toClan.getOwner());

        //Travelling from wilderness
        if(fromClan == null || !fromClan.getUuid().equals(toClan.getUuid())) {
            e.getPlayer().sendTitle(ChatColor.YELLOW + toClan.getName(), "Owned By " + ChatColor.BLUE + owner.getName(), 5, 50, 5);
            return;
        }
    }
}
