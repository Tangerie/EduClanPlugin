package xyz.tangerie.educlan.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import xyz.tangerie.educlan.managers.ClansManager;
import xyz.tangerie.educlan.models.ECClan;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;

        if(e.getDamager().getType() == EntityType.PLAYER) {
            ECClan clan = ClansManager.getInstance().getClanByChunk(e.getEntity().getChunk());

            if(clan == null) return;

            if(e.getEntity().getType() == EntityType.PLAYER) {
                if(!clan.isPVPAllowed())  {
                    e.setCancelled(true);
                    ((Player)e.getDamager()).sendActionBar(ChatColor.RED + "PVP not allowed in clan's land");
                }
            } else {
                if(!clan.isPVEAllowed())  {
                    e.setCancelled(true);
                    ((Player)e.getDamager()).sendActionBar(ChatColor.RED + "PVE not allowed in clan's land");
                }
            }
        } else if(e.getDamager().getType() == EntityType.ARROW || e.getDamager().getType() == EntityType.SPECTRAL_ARROW || e.getDamager().getType() == EntityType.SPLASH_POTION) {
            ECClan clan = ClansManager.getInstance().getClanByChunk(e.getEntity().getChunk());

            if(clan == null) return;

            if(e.getEntity().getType() == EntityType.PLAYER) {
                if(!clan.isPVPAllowed())  {
                    e.setCancelled(true);
                }
            } else {
                if(!clan.isPVEAllowed())  {
                    e.setCancelled(true);
                }
            }
        }
    }
}
