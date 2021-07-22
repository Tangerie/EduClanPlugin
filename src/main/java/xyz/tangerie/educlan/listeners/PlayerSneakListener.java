package xyz.tangerie.educlan.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import xyz.tangerie.educlan.EduClan;

public class PlayerSneakListener implements Listener {

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        //EduClan.dynmapAPI.setPlayerVisiblity(e.getPlayer().getName(), !e.isSneaking());
    }
}
