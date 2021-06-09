package xyz.tangerie.educlan.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.tangerie.educlan.managers.ClansManager;
import xyz.tangerie.educlan.models.ECClan;

public class BlockInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        switch(e.getAction()) {
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
            case PHYSICAL:
                return;
        }



        ECClan clan = ClansManager.getInstance().getClanByChunk(e.getClickedBlock().getChunk());

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            clan = ClansManager.getInstance().getClanByChunk(e.getInteractionPoint().add(e.getBlockFace().getDirection()).getChunk());
        }
        if(clan == null || clan.isPlayerInClan(e.getPlayer())) return;

        e.getPlayer().sendRawMessage(ChatColor.RED + "You are not a member of this clan");
        e.setCancelled(true);
    }
}
