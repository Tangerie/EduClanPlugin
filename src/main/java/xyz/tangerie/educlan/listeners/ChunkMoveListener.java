package xyz.tangerie.educlan.listeners;

import net.kyori.adventure.bossbar.BossBar;
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
            e.getPlayer().sendActionBar(ChatColor.DARK_GREEN + "Wilderness");
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(toClan.getOwner());

        //Travelling from wilderness
        if(fromClan == null || !fromClan.getUuid().equals(toClan.getUuid())) {
            e.getPlayer().sendActionBar(ChatColor.GOLD + toClan.getName() + ChatColor.WHITE + ChatColor.BLUE + " [" + owner.getName() + "]");
            if(!toClan.isPlayerInClan(e.getPlayer()) && toClan.messageOwnerOnEntry()) {
                toClan.sendOwnerActionBar(ChatColor.DARK_RED + e.getPlayer().getName() + " Entered Your Land");
            }
            return;
        }
    }
}
