package xyz.tangerie.educlan.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import xyz.tangerie.educlan.EduClan;
import xyz.tangerie.educlan.files.ClansFile;
import xyz.tangerie.educlan.models.*;

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

    //private Map<UUID, Long> playerLastTeleportTime = new HashMap<>();

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
        if(!chunk.getWorld().getName().equalsIgnoreCase("world")) {
            return null;
        }
        return clans.stream().filter(x -> x.isChunkInClan(chunk.getChunkKey())).findAny().orElse(null);
    }

    public ECClan getClanByChunk(long id) {
        //Check if chunk is in the overworld
        return clans.stream().filter(x -> x.isChunkInClan(id)).findAny().orElse(null);
    }

    public void claimChunk(Chunk chunk, ECClan playerClan) {
        if(playerClan == null) return;

        ECClan clan = getClanByChunk(chunk);
        if(clan != null) return;

        addMarkerForChunk(chunk, playerClan);

        ClansFile.addChunkToClan(playerClan, chunk);
        reloadFromFile();
    }

    private boolean areChunksTouching(Chunk a, Chunk b) {
       int x = a.getX() - b.getX();
       int z = a.getZ() - b.getZ();

       if(x*x + z*z == 1) {
           return true;
       }
       return false;
    }

    public List<ECClan> clansTouchingChunk(Chunk c) {
        List<ECClan> clans = new ArrayList<>();

        /*for(ECClan clan : getClans()) {
            for(long key : clan.getChunks()) {
                Chunk c2 = Bukkit.getWorld("world").getChunkAt(key);
                if(c2 != null) {
                    if(areChunksTouching(c, c2)) {
                        clans.add(clan);
                        break;
                    }
                }
            }
        }*/
        World world = Bukkit.getWorld("world");
        Chunk topC = world.getChunkAt(c.getX(), c.getZ() + 1);
        Chunk bottomC = world.getChunkAt(c.getX(), c.getZ() - 1);
        Chunk leftC = world.getChunkAt(c.getX() + 1, c.getZ());
        Chunk rightC = world.getChunkAt(c.getX() - 1, c.getZ());

        if(topC != null) {
            ECClan clan = getClanByChunk(topC);
            if(clan != null && !clans.contains(clan)) {
                clans.add(clan);
            }
        }

        if(bottomC != null) {
            ECClan clan = getClanByChunk(bottomC);
            if(clan != null && !clans.contains(clan)) {
                clans.add(clan);
            }
        }

        if(leftC != null) {
            ECClan clan = getClanByChunk(leftC);
            if(clan != null && !clans.contains(clan)) {
                clans.add(clan);
            }
        }

        if(rightC != null) {
            ECClan clan = getClanByChunk(rightC);
            if(clan != null && !clans.contains(clan)) {
                clans.add(clan);
            }
        }



        return clans;
    }

    public boolean areClanClaimsValid(ECClan clan) {
        if(clan.getChunks().size() > 1) {
            World world = Bukkit.getWorld("world");

            List<Chunk> startingPoints = new LinkedList<>();

            if(clan.getBeacons().size() > 0) {
                for(Long b : clan.getBeacons()) {
                    Block bl = world.getBlockAtKey(b);

                    startingPoints.add(world.getChunkAt(bl));
                }
            } else {
                startingPoints.add(world.getChunkAt(clan.getChunks().get(0)));
            }


            List<Long> seenChunks = new ArrayList<>();

            for(Chunk c : startingPoints) {
                stepClanChunk(clan, c, seenChunks);
            }

            return seenChunks.size() == clan.getChunks().size();
        }

        return true;
    }

    private void stepClanChunk(ECClan clan, Chunk chunk, List<Long> seenChunks) {
        World world = Bukkit.getWorld("world");
        if(seenChunks.contains(chunk.getChunkKey())) {
            return;
        } else {
            seenChunks.add(chunk.getChunkKey());
        }

        long below = Chunk.getChunkKey(chunk.getX(), chunk.getZ() + 1);
        long right = Chunk.getChunkKey(chunk.getX() + 1, chunk.getZ());
        long left = Chunk.getChunkKey(chunk.getX() - 1, chunk.getZ());
        long up = Chunk.getChunkKey(chunk.getX(), chunk.getZ() - 1);

        if(getClanByChunk(below) != null) {
            stepClanChunk(clan, world.getChunkAt(below), seenChunks);
        }

        if(getClanByChunk(right) != null) {
            stepClanChunk(clan, world.getChunkAt(right), seenChunks);
        }

        if(getClanByChunk(left) != null) {
            stepClanChunk(clan, world.getChunkAt(left), seenChunks);
        }

        if(getClanByChunk(up) != null) {
            stepClanChunk(clan, world.getChunkAt(up), seenChunks);
        }
    }

    private void addMarkerForChunk(Chunk ch, ECClan clan) {
        String mId = "world." + ch.getChunkKey();
        double[] cx = {ch.getX() * 16, ch.getX() * 16 + 16};
        double[] cz = {ch.getZ() * 16, ch.getZ() * 16 + 16};
        AreaMarker am = EduClan.markerset.createAreaMarker(mId, clan.getName(), false, "world", cx, cz, false);
        am.setLabel(clan.getName());
        am.setDescription(clan.getName());
        am.setFillStyle(0.4, clan.getClanColor());
        am.setLineStyle(0, 0.0, 0);
    }

    private void removeClanHomeMarker(ECClan clan) {
        String mId = "world." + clan.getName() + ".home";
        Marker m = EduClan.markerset.findMarker(mId);
        if(m != null) {
            m.deleteMarker();
        }
    }

    private void addClanHomeMarker(ECClan clan) {
        Location loc = clan.getHomeLocation();
        String mId = "world." + clan.getName() + ".home";
        Marker m = EduClan.markerset.createMarker(mId, clan.getName() + " Home", false, "world", loc.getX(), loc.getY(), loc.getZ(), EduClan.mapi.getMarkerIcon(MarkerIcon.DEFAULT), false);
    }

    public void regenerateDynmapMarkers() {
        for(ECClan clan : getClans()) {
            for(long key : clan.getChunks()) {
                Chunk c = Bukkit.getWorld("world").getChunkAt(key);
                if(c != null) {
                    addMarkerForChunk(c, clan);
                }
            }

            if(clan.getHomeLocation() != null) {
                addClanHomeMarker(clan);
            }
        }
    }

    public void unclaimChunk(Chunk chunk, Player p) {
        ECClan clan = getClanByChunk(chunk);
        if(clan == null || !clan.getOwner().equals(p.getUniqueId())) {
            return;
        }

        String mId = "world." + chunk.getChunkKey();
        AreaMarker am = EduClan.markerset.findAreaMarker(mId);
        if(am != null) {
            am.deleteMarker();
        }

        ClansFile.removeChunkFromClan(clan, chunk);
        reloadFromFile();
    }

    public void unclaimChunk(Chunk chunk, UUID p) {
        ECClan clan = getClanByChunk(chunk);
        if(clan == null || !clan.getOwner().equals(p)) {
            return;
        }

        String mId = "world." + chunk.getChunkKey();
        AreaMarker am = EduClan.markerset.findAreaMarker(mId);
        if(am != null) {
            am.deleteMarker();
        }

        ClansFile.removeChunkFromClan(clan, chunk);
        reloadFromFile();
    }

    public void disbandClan(ECClan c) {
        for(long key : c.getChunks()) {
            String mId = "world." + key;;
            AreaMarker am = EduClan.markerset.findAreaMarker(mId);
            if(am != null) {
                am.deleteMarker();
            }
        }
        removeClanHomeMarker(c);
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

    public void setClanHome(ECClan clan, Location loc) {
        if(clan.getHomeLocation() != null) {
            removeClanHomeMarker(clan);
        }
        ClansFile.setClanHome(clan, loc);
        reloadFromFile();
        addClanHomeMarker(getClanByName(clan.getName()));
    }

    public void removeClanHome(ECClan clan) {
        if(clan.getHomeLocation() == null) {
            return;
        }

        removeClanHomeMarker(clan);
        ClansFile.removeClanHome(clan);
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

    public void setChunkLimit(ECClan clan, int limit) {
        ClansFile.setChunkLimit(clan, limit);
        reloadFromFile();
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
        }
    }

    public void claimBeacon(ECClan clan, Block beacon) {
        ClansFile.addBeaconToClan(clan, beacon.getBlockKey());
        reloadFromFile();
    }
}
