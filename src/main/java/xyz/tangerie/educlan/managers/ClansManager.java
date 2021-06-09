package xyz.tangerie.educlan.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import xyz.tangerie.educlan.files.ClansFile;
import xyz.tangerie.educlan.models.*;
import xyz.tangerie.edulib.shop.InventoryHelper;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;

public class ClansManager {
    private static ClansManager instance;

    public static ClansManager getInstance() {
        if(instance == null) {
            instance = new ClansManager();
        }

        return instance;
    }

    private List<ECClan> clans;

    public ClansManager() {
        reloadFromFile();
    }

    public void reloadFromFile() {
        getLogger().info("Reloading Clans From File");
        clans = ClansFile.getClans();
        getLogger().info(clans.size() + " Clans Loaded From File");
    }

    public List<ECClan> getClans() {
        return clans;
    }

    public ECClan createClan(Player p, String name) {
        if(getClanByName(name) != null) {
            return null;
        }
        ECClan clan = new ECClan(name, p);
        ClansFile.addClan(clan);
        clans.add(clan);

        return clan;
    }

    public List<ECClan> getPlayerClans(Player p) {
        return clans.stream().filter(x -> x.isPlayerInClan(p)).collect(Collectors.toList());
    }

    public ECClan getClanByName(String name) {
        return clans.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public void removePlayerFromClan(Player p, ECClan c) {
        if(!c.getOwner().equals(p.getUniqueId())) {
            ClansFile.removePlayerFromClan(p, c);
            reloadFromFile();
        } else {
            getLogger().info("Tried to remove owner from own clan?");
        }
    }

    public ECClan getClanByOwner(Player owner) {
        return clans.stream().filter(x -> x.getOwner().equals(owner.getUniqueId())).findAny().orElse(null);
    }

    public ECClan getClanByChunk(Chunk chunk) {
        //Check if chunk is in the overworld
        return clans.stream().filter(x -> x.isChunkInClan(chunk.getChunkKey())).findAny().orElse(null);
    }

    public void claimChunk(Chunk chunk, Player p) {
        ECClan playerClan = getClanByOwner(p);
        if(playerClan == null) return;

        ECClan clan = getClanByChunk(chunk);
        if(clan != null) return;


        ClansFile.addChunkToClan(playerClan, p.getChunk());
        reloadFromFile();
    }

    public void unclaimChunk(Chunk chunk, Player p) {
        ECClan clan = getClanByChunk(chunk);
        if(clan == null || !clan.getOwner().equals(p.getUniqueId())) {
            return;
        }

        ClansFile.removeChunkFromClan(clan, chunk);
        reloadFromFile();
    }

    public void disbandClan(ECClan c) {
        ClansFile.removeClan(c);
        reloadFromFile();
    }

    public void setClanName(ECClan c, String newName) {
        ClansFile.setClanName(c, newName);
        reloadFromFile();
    }

    public void setClanSetting(ECClan c, String key, Object value) {
        ClansFile.setClanSetting(c, key, value);
        reloadFromFile();
    }

    public void invitePlayerToClan(ECClan clan, Player p) {
        if(!clan.isPlayerInClan(p) && !clan.isPlayerInvitee(p)) {
            ClansFile.addInvitee(clan, p);
            reloadFromFile();
        }
    }

    public void acceptInvitation(ECClan clan, Player p) {
        if(clan.isPlayerInvitee(p)) {
            ClansFile.removeInvitee(clan, p);
            ClansFile.addPlayerToClan(clan, p);
            reloadFromFile();
        }
    }

    public void kickMember(ECClan clan, Player p) {
        if(clan.isPlayerInvitee(p)) {
            ClansFile.removeInvitee(clan, p);
            reloadFromFile();
            return;
        }

        if(clan.isPlayerInClan(p)) {
            ClansFile.removePlayerFromClan(p, clan);
            reloadFromFile();
            return;
        }
    }

    public static Inventory getClanManager(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9 * 5, "Clan Manager");

        inv.setItem(0, InventoryHelper.createGUIItem());

        return inv;
    }
}
