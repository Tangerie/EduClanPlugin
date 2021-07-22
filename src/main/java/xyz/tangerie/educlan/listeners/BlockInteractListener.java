package xyz.tangerie.educlan.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import xyz.tangerie.educlan.Util;
import xyz.tangerie.educlan.managers.ClansManager;
import xyz.tangerie.educlan.models.ECClan;

import java.util.List;

public class BlockInteractListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        switch(e.getAction()) {
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
            case PHYSICAL:
                return;
        }

        ECClan clan = ClansManager.getInstance().getClanByChunk(e.getClickedBlock().getChunk());

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(e.getInteractionPoint() == null) {
                return;
            }

            clan = ClansManager.getInstance().getClanByChunk(e.getInteractionPoint().add(e.getBlockFace().getDirection()).getChunk());
        }
        if(clan == null || clan.isPlayerInClan(e.getPlayer())) return;

        e.getPlayer().sendActionBar(ChatColor.RED + "You are not a member of this clan");
        e.setCancelled(true);
    }

    @EventHandler
    public void checkNetherStarClick(PlayerInteractEvent e) {
        if(e.isCancelled() || e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player p = e.getPlayer();


        ItemStack used = e.getItem();
        Block clicked = e.getClickedBlock();

        if(used != null && clicked != null && used.getType().equals(Material.NETHER_STAR) && clicked.getType().equals(Material.BEACON)) {
            e.setCancelled(true);

            ECClan clan = ClansManager.getInstance().getClanByOwner(p);
            if(clan == null) {
                p.sendRawMessage(ChatColor.RED + "You must own a clan");
                return;
            }

            ECClan otherClan = ClansManager.getInstance().getClanByChunk(clicked.getChunk());

            if(otherClan != null && otherClan != clan) {
                p.sendRawMessage(ChatColor.RED + "Beacons cannot be created in another clans land");
                return;
            }

            Beacon bea = (Beacon)clicked.getState();

            long key = clicked.getLocation().toBlockKey();

            if(clan.isBeaconOwned(key)) {
                p.sendRawMessage(ChatColor.RED + "Already active");
                return;
            }

            //Created in an empty land
            if(otherClan == null) {
                if(clan.getBeacons().size() == 0) {
                    p.sendRawMessage(ChatColor.RED + "You must have a claim beacon in your pre-existing land");
                    return;
                }

                List<ECClan> clansTouching = ClansManager.getInstance().clansTouchingChunk(clicked.getChunk());
                if(clansTouching.size() > 1) {
                    p.sendRawMessage("Must be a valid claim");
                    return;
                }

                if(clansTouching.size() == 1 && clansTouching.get(0) != clan) {
                    p.sendRawMessage("Cannot be touching another clan");
                    return;
                }
            }

            if(clan.getChunks().size() + 1 > clan.getChunkLimit()) {
                p.sendRawMessage(ChatColor.RED + "Reached chunk limit");
                return;
            }

            p.sendRawMessage( ChatColor.YELLOW + "Claim beacon created");
            ClansManager.getInstance().claimBeacon(clan, clicked);
            ClansManager.getInstance().claimChunk(clicked.getChunk(), clan);
            p.getInventory().removeItemAnySlot(new ItemStack(Material.NETHER_STAR, 1));

        }
    }

    @EventHandler
    public void checkBeaconBreak(BlockBreakEvent e) {
        if(e.isCancelled()) {
            return;
        }

        if(e.getBlock().getType() == Material.BEACON) {
            ECClan clan = ClansManager.getInstance().getClanByChunk(e.getBlock().getChunk());
            if(clan != null && clan.isBeaconOwned(e.getBlock())) {
                e.setCancelled(true);
                e.getPlayer().sendRawMessage(ChatColor.RED + "Cannot break claim beacon");
                return;
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        for(Block b : e.blockList()) {
            ECClan c = ClansManager.getInstance().getClanByChunk(b.getChunk());
            if(c != null) {
                c.sendOwnerActionBar(ChatColor.DARK_AQUA +  "Explosion was detected in your land");
                e.setCancelled(true);
                return;
            }
        }
    }
}
