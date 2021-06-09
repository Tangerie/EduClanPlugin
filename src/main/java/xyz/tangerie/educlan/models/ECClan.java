package xyz.tangerie.educlan.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ECClan {
    private UUID uuid;
    private List<UUID> players;
    private List<UUID> invitees;
    private List<Long> chunks;
    private String name;
    private UUID owner;
    private Map<?, ?> settings;

    public ECClan(UUID uuid, List<UUID> players, List<UUID> invitees, List<Long> chunks, String name, UUID owner, Map<?, ?> settings) {
        this.uuid = uuid;
        this.players = players;
        this.invitees = invitees;
        this.chunks = chunks;
        this.name = name;
        this.owner = owner;
        this.settings = settings;
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

    private void messagePlayerIfOnline(UUID id, String msg) {
        if(Bukkit.getOfflinePlayer(id).isOnline()) {
            Bukkit.getPlayer(id).sendRawMessage(msg);
        }
    }

    public void messageOwner(String msg) {
        messagePlayerIfOnline(getOwner(), msg);
    }

    public void messageClan(String msg) {
        for(UUID u : getPlayers()) {
            messagePlayerIfOnline(u, msg);
        }
    }
}
