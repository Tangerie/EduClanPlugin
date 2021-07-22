package xyz.tangerie.educlan.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xyz.tangerie.educlan.EduClan;

import java.util.*;
import org.bukkit.plugin.java.JavaPlugin;

public class ECClan {
    private UUID uuid;
    private List<UUID> players;
    private List<UUID> invitees;
    private List<Long> chunks;
    private String name;
    private UUID owner;
    private Map<?, ?> settings;
    private int chunkLimit;
    private Location homeLocation;
    private List<Long> beacons;

    public ECClan(UUID uuid, List<UUID> players, List<UUID> invitees, List<Long> chunks, String name, UUID owner, Map<?, ?> settings, int chunkLimit, Location homeLocation, List<Long> beacons) {
        this.uuid = uuid;
        this.players = players;
        this.invitees = invitees;
        this.chunks = chunks;
        this.name = name;
        this.owner = owner;
        this.settings = settings;
        this.chunkLimit = chunkLimit;
        this.homeLocation = homeLocation;
        this.beacons = beacons;
    }

    public ECClan(String name, Player p) {
        this.uuid = UUID.randomUUID();
        this.players = new LinkedList<>();
        this.players.add(p.getUniqueId());
        this.name = name;
        this.owner = p.getUniqueId();
        this.chunks = new LinkedList<>();
        this.settings = new HashMap<String, Object>();
        this.invitees = new LinkedList<>();
        this.chunkLimit = EduClan.config.getInt("startingChunkMax");
        this.homeLocation = null;
        this.beacons = new LinkedList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public boolean isPlayerInClan(Player p) {
        return players.contains(p.getUniqueId());
    }

    public void addPlayer(Player p) {
        players.add(p.getUniqueId());
    }

    public void removePlayer(Player p) {
        players.remove(p.getUniqueId());
    }

    public void removePlayer(UUID u) {
        players.remove(u);
    }

    public List<Long> getChunks() {
        return chunks;
    }

    public void addChunk(long l) {
        chunks.add(l);
    }

    public void removeChunk(long l) {
        chunks.remove(l);
    }

    public boolean isChunkInClan(long c) {
        return chunks.contains(c);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPlayerInvitee(Player p) { return invitees.contains(p.getUniqueId()); }

    public List<UUID> getInvitees() {
        return invitees;
    }

    public Map<?, ?> getSettings() {
        return settings;
    }

    public Object getSettingValue(String key, Object defVal) {
        if(settings.containsKey(key)) {
            return settings.get(key);
        } else {
            return defVal;
        }
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isPVPAllowed() {
        return (boolean)getSettingValue("pvp", true);
    }

    public boolean isPVEAllowed() {
        return (boolean)getSettingValue("pve", true);
    }

    public int getClanColor() {
        return (int)getSettingValue("color", 0xf54242);
    }

    public boolean messageOwnerOnEntry() {
        return (boolean)getSettingValue("msg", false);
    }

    private void messagePlayerIfOnline(UUID id, String msg) {
        if(Bukkit.getOfflinePlayer(id).isOnline()) {
            Bukkit.getPlayer(id).sendRawMessage(msg);
        }
    }

    private void sendActionBarPlayerIfOnline(UUID id, String msg) {
        if(Bukkit.getOfflinePlayer(id).isOnline()) {
            Bukkit.getPlayer(id).sendActionBar(msg);
        }
    }

    public void messageOwner(String msg) {
        messagePlayerIfOnline(getOwner(), msg);
    }

    public void sendOwnerActionBar(String msg) {
        sendActionBarPlayerIfOnline(getOwner(), msg);
    }

    public void messageClan(String msg) {
        for(UUID u : getPlayers()) {
            messagePlayerIfOnline(u, msg);
        }
    }

    public int getChunkLimit() {
        return chunkLimit + ((getPlayers().size() - 1) * 5);
    }

    public int getRawChunkLimit() {
        return chunkLimit;
    }

    public void setChunkLimit(int chunkLimit) {
        this.chunkLimit = chunkLimit;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }

    public boolean isBeaconOwned(long key) {
        return beacons.contains(key);
    }

    public boolean isBeaconOwned(Block b) {
        return isBeaconOwned(b.getBlockKey());
    }

    public List<Long> getBeacons() {
        return beacons;
    }
}
