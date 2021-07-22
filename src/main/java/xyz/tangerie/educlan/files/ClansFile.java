package xyz.tangerie.educlan.files;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.tangerie.educlan.models.ECClan;

import java.util.*;
import java.util.stream.Collectors;

public class ClansFile extends ConfigFile {
    private static ClansFile config;

    public static ClansFile getConfig() {
        if(config == null) {
            config = new ClansFile();
        }
        return config;
    }

    public ClansFile() {
        super("clans.yml");
    }

    public static List<ECClan> getClans() {
        LinkedList<ECClan> clans = new LinkedList<>();
        for(String clanU : getConfig().getConfigurationSection("clans").getKeys(false)) {
            String path = "clans." + clanU;
            UUID clanUUID = UUID.fromString(clanU);

            String name = getConfig().getString(path + ".name");
            UUID owner = UUID.fromString(getConfig().getString(path + ".owner"));
            List<UUID> players = getConfig().getStringList(path + ".players").stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            List<UUID> invitees = getConfig().getStringList(path + ".invitees").stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            List<Long> chunks = getConfig().getLongList(path + ".chunks");

            List<Long> beacons = getConfig().getLongList(path + ".beacons");

            if(beacons == null) {
                beacons = new LinkedList<>();
            }

            int chunkLimit = getConfig().getInt(path + ".chunkLimit");

            Location homeLoc = null;

            ConfigurationSection homeLocSec = getConfig().getConfigurationSection(path + ".homeLocation");

            if(homeLocSec != null) {
                homeLoc = new Location(Bukkit.getWorld("world"), homeLocSec.getDouble("x"), homeLocSec.getDouble("y"), homeLocSec.getDouble("z"));
            }

            ConfigurationSection setConfig = getConfig().getConfigurationSection(path + ".settings");

            Map<?, ?> settings = new HashMap<String, Object>();
            if(setConfig != null) {
                settings = setConfig.getValues(true);
            }

            clans.add(new ECClan(clanUUID, players, invitees, chunks, name, owner, settings, chunkLimit, homeLoc, beacons));
        }
        return clans;
    }

    public static void addClan(ECClan clan) {
        //Add it
        getConfig().getConfigurationSection("clans").createSection(clan.getUuid().toString());
        String path = "clans." + clan.getUuid().toString();

        getConfig().getConfigurationSection(path).set("name", clan.getName());
        getConfig().getConfigurationSection(path).set("owner", clan.getOwner().toString());
        getConfig().getConfigurationSection(path).set("players", clan.getPlayers().stream()
                .map(UUID::toString)
                .collect(Collectors.toList()));
        getConfig().getConfigurationSection(path).set("invitees", clan.getInvitees().stream()
                .map(UUID::toString)
                .collect(Collectors.toList()));
        getConfig().getConfigurationSection(path).set("chunks", clan.getChunks());
        getConfig().getConfigurationSection(path).set("settings", clan.getSettings());
        getConfig().getConfigurationSection(path).set("chunkLimit", clan.getChunkLimit());

        if(clan.getHomeLocation() != null) {
            getConfig().getConfigurationSection(path).createSection("homeLocation");
            ConfigurationSection homeLoc = getConfig().getConfigurationSection(path).getConfigurationSection("homeLocation");
            homeLoc.set("x", clan.getHomeLocation().getX());
            homeLoc.set("y", clan.getHomeLocation().getY());
            homeLoc.set("z", clan.getHomeLocation().getZ());
        }

        getConfig().getConfigurationSection(path).set("beacons", clan.getBeacons());

        getConfig().save();
    }

    public static void removePlayerFromClan(Player p, ECClan clan) {
        if(getConfig().contains("clans." + clan.getUuid().toString())) {
            List<String> uuids = getConfig().getStringList("clans." + clan.getUuid().toString() + ".players");
            uuids.remove(p.getUniqueId().toString());
            getConfig().set("clans." + clan.getUuid().toString() + ".players", uuids);

            getConfig().save();
        }
    }

    public static void addPlayerToClan(ECClan clan, Player p) {
        if(getConfig().contains("clans." + clan.getUuid().toString())) {
            List<String> uuids = getConfig().getStringList("clans." + clan.getUuid().toString() + ".players");
            uuids.add(p.getUniqueId().toString());
            getConfig().set("clans." + clan.getUuid().toString() + ".players", uuids);

            getConfig().save();
        }
    }

    public static void addInvitee(ECClan clan, Player p) {
        if(getConfig().contains("clans." + clan.getUuid().toString())) {
            List<String> uuids = getConfig().getStringList("clans." + clan.getUuid().toString() + ".invitees");
            uuids.add(p.getUniqueId().toString());
            getConfig().set("clans." + clan.getUuid().toString() + ".invitees", uuids);

            getConfig().save();
        }
    }

    public static void removeInvitee(ECClan clan, Player p) {
        if(getConfig().contains("clans." + clan.getUuid().toString())) {
            List<String> uuids = getConfig().getStringList("clans." + clan.getUuid().toString() + ".invitees");
            uuids.remove(p.getUniqueId().toString());
            getConfig().set("clans." + clan.getUuid().toString() + ".invitees", uuids);

            getConfig().save();
        }
    }

    public static void addChunkToClan(ECClan clan, Chunk chunk) {
        ConfigurationSection section = getConfig().getConfigurationSection("clans." + clan.getUuid().toString());
        if(section != null) {
            List<Long> uuids = section.getLongList("chunks");
            uuids.add(chunk.getChunkKey());
            section.set("chunks", uuids);

            getConfig().save();
        }
    }

    public static void removeChunkFromClan(ECClan clan, Chunk chunk) {
        ConfigurationSection section = getConfig().getConfigurationSection("clans." + clan.getUuid().toString());
        if(section != null) {
            List<Long> uuids = section.getLongList("chunks");
            uuids.remove(chunk.getChunkKey());
            section.set("chunks", uuids);

            getConfig().save();
        }
    }

    public static void setChunkLimit(ECClan clan, int limit) {
        ConfigurationSection section = getConfig().getConfigurationSection("clans." + clan.getUuid().toString());
        if(section != null) {
            section.set("chunkLimit", limit);
            getConfig().save();
        }
    }

    public static void removeClan(ECClan clan) {
        getConfig().set("clans." + clan.getUuid().toString(), null);
        getConfig().save();
    }

    public static void setClanName(ECClan clan, String newName) {
        ConfigurationSection section = getConfig().getConfigurationSection("clans." + clan.getUuid().toString());
        if(section != null) {
            section.set("name", newName);
            getConfig().save();
        }
    }

    public static void setClanSetting(ECClan clan, String key, Object value) {
        ConfigurationSection section = getConfig().getConfigurationSection("clans." + clan.getUuid().toString());
        if(section != null) {
            if(section.getConfigurationSection("settings") == null) {
                section.createSection("settings");
            }
            section.getConfigurationSection("settings").set(key, value);
            getConfig().save();
        }
    }

    public static void setClanHome(ECClan clan, Location loc) {
        String path = "clans." + clan.getUuid().toString();

        if(getConfig().getConfigurationSection(path).getConfigurationSection("homeLocation") == null) {
            getConfig().getConfigurationSection(path).createSection("homeLocation");
        }

        ConfigurationSection homeLoc = getConfig().getConfigurationSection(path).getConfigurationSection("homeLocation");

        homeLoc.set("x", loc.getX());
        homeLoc.set("y", loc.getY());
        homeLoc.set("z", loc.getZ());

        getConfig().save();
    }

    public static void removeClanHome(ECClan clan) {
        String path = "clans." + clan.getUuid().toString();

        getConfig().getConfigurationSection(path).set("homeLocation", null);

        getConfig().save();
    }

    public static void addBeaconToClan(ECClan clan, long key) {
        String path = "clans." + clan.getUuid().toString();
        if(getConfig().contains(path)) {
            List<Long> beas = getConfig().getLongList(path + ".beacons");
            beas.add(key);
            getConfig().set(path + ".beacons", beas);

            getConfig().save();
        }
    }
}
