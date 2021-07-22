package xyz.tangerie.educlan.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.tangerie.educlan.EduClan;
import xyz.tangerie.educlan.managers.*;
import xyz.tangerie.educlan.models.*;
import xyz.tangerie.edulib.shop.InventoryHelper;

import java.util.*;
import java.util.regex.*;

import static org.bukkit.Bukkit.getLogger;

@CommandAlias("clan")
public class ClanCommand extends BaseCommand {

    @Subcommand("list")
    @Description("List all clans")
    public static void listClans(Player p) {
        p.sendRawMessage(String.format("---- All [%d] Clans ----", ClansManager.getInstance().getClans().size()));
        for(ECClan c : ClansManager.getInstance().getClans()) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(c.getOwner());
            p.sendRawMessage(ChatColor.YELLOW + c.getName() + ChatColor.BLUE + " [" + owner.getName() + "] " + ChatColor.RESET + "(" + c.getChunks().size() + "/" + c.getChunkLimit() + " Chunks Claimed)");
            for(UUID u : c.getPlayers()) {
                p.sendRawMessage("    - " + Bukkit.getOfflinePlayer(u).getName());
            }
        }
    }

    @Subcommand("list")
    @CommandCompletion("@players")
    @Description("List a player's clans")
    @Syntax("<player>")
    public static void getMyClans(Player p, String otherPlayer) {
        Player other = Bukkit.getPlayer(otherPlayer);
        if(other == null) {
            p.sendRawMessage(ChatColor.RED + "Invalid Player");
            return;
        }
        List<ECClan> clans = ClansManager.getInstance().getPlayerClans(other);
        p.sendRawMessage(String.format("---- %s's [%d] Clan(s) ----", other.getName(), clans.size()));
        for(ECClan c : clans) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(c.getOwner());
            p.sendRawMessage(ChatColor.YELLOW + c.getName() + ChatColor.BLUE + " [" + owner.getName() + "] " + ChatColor.RESET + "(" + c.getChunks().size() + "/" + c.getChunkLimit() + " Chunks Claimed)");
            for(UUID u : c.getPlayers()) {
                p.sendRawMessage("    - " + Bukkit.getOfflinePlayer(u).getName());
            }
        }
    }

    @Subcommand("create")
    @Description("Create a clan")
    @Conditions("!ownsclan")
    public static void createClan(Player p, String name) {
        if(ClansManager.getInstance().getClanByName(name) != null) {
            p.sendRawMessage(ChatColor.RED + "Clan with name already exists.");
            return;
        }

        ECClan clan = ClansManager.getInstance().createClan(p, name);
        p.sendRawMessage(ChatColor.GREEN + "Clan created");
    }

    @Subcommand("leave")
    @Description("Leave a clan")
    @CommandCompletion("@playerclans")
    @Syntax("<clan>")
    public static void leaveClan(Player p, String name) {
        ECClan clan = ClansManager.getInstance().getClanByName(name);

        if(clan == null) {
            p.sendRawMessage(ChatColor.RED + "Clan does not exist");
            return;
        }

        if(clan.getOwner().equals(p.getUniqueId())) {
            p.sendRawMessage(ChatColor.RED + "You cannot leave your own clan");
            p.sendRawMessage(ChatColor.YELLOW + "You may disband it with /clan disband");
            return;
        }

        ClansManager.getInstance().removePlayerFromClan(p, clan);
        p.sendRawMessage("Left " + clan.getName());
        clan.messageClan(ChatColor.RED + p.getName() + ChatColor.WHITE + " Left " + ChatColor.YELLOW + clan.getName());
    }

    @Subcommand("claim")
    @Description("Claim the current chunk")
    @Conditions("ownsclan")
    public static void claimChunk(Player p) {
        //Can claim chunk
        ECClan clan = ClansManager.getInstance().getClanByChunk(p.getLocation().getChunk());
        if(clan != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(clan.getOwner());
            p.sendRawMessage("You cannot claim this land as it belongs to " + ChatColor.YELLOW + clan.getName() + ChatColor.BLUE + " [" + owner.getName() + "]");
            return;
        }

        ECClan playerClan = ClansManager.getInstance().getClanByOwner(p);

        if(playerClan.getChunks().size() == playerClan.getChunkLimit()) {
            p.sendRawMessage(ChatColor.RED + "All " + playerClan.getChunkLimit() + " chunks have been claimed");
            return;
        }

        List<ECClan> touching = ClansManager.getInstance().clansTouchingChunk(p.getLocation().getChunk());

        if(touching.size() > 1) {
            p.sendRawMessage(ChatColor.RED + "Chunk may not be touching another clan");
            return;
        }

        if((touching.size() == 1 && touching.get(0).getUuid() == playerClan.getUuid()) || (touching.size() == 0 && playerClan.getChunks().size() == 0)) {
            ClansManager.getInstance().claimChunk(p.getLocation().getChunk(), playerClan);
            p.sendRawMessage(ChatColor.GREEN + "Claimed Chunk");
            return;
        } else if(touching.size() == 1) {
            p.sendRawMessage(ChatColor.RED + "Chunk must be adjacent to your own");
        }

        if(touching.size() == 0) {
            p.sendRawMessage(ChatColor.RED + "Chunk must be adjacent to your own");
        }
    }

    @Subcommand("unclaim")
    @Description("Unclaim the current chunk")
    @Conditions("ownsclan")
    public static void unclaimChunk(Player p) {
        ECClan clan = ClansManager.getInstance().getClanByChunk(p.getLocation().getChunk());
        if(clan == null || !clan.getOwner().equals(p.getUniqueId())) {
            p.sendRawMessage(ChatColor.RED + "You do not own this chunk");
            return;
        }

        World world = Bukkit.getWorld("world");

        for(Long k : clan.getBeacons()) {
            Chunk c = world.getChunkAt(world.getBlockAtKey(k));
            if(c == p.getLocation().getChunk()) {
                p.sendRawMessage(ChatColor.RED + "Cannot unclaim chunk with a claim beacon");
                return;
            }
        }

        ClansManager.getInstance().unclaimChunk(p.getLocation().getChunk(), p);

        clan = ClansManager.getInstance().getClanByOwner(p);

        boolean valid = ClansManager.getInstance().areClanClaimsValid(clan);

        if(!valid) {
            ClansManager.getInstance().claimChunk(p.getLocation().getChunk(), clan);
            p.sendRawMessage(ChatColor.RED + "Invalid Unclaim");
            return;
        }

        if(clan.getHomeLocation() != null && p.getLocation().getChunk().getChunkKey() == clan.getHomeLocation().getChunk().getChunkKey()) {
            ClansManager.getInstance().removeClanHome(clan);
        }

        p.sendRawMessage(ChatColor.GREEN + "Unclaimed Chunk");
    }

    @Subcommand("disband")
    @Description("Disband your clan")
    @Conditions("ownsclan")
    public static void disbandClan(Player p) {
        ECClan clan = ClansManager.getInstance().getClanByOwner(p);

        //Give owner diamonds
        int chunksBought = clan.getRawChunkLimit() - EduClan.config.getInt("startingChunkMax");

        int numDiamonds = 0;
        for(int i = 1; i <= chunksBought; i++) {
            numDiamonds += calculateDiamondsRequired(i);
        }

        p.getInventory().addItem(new ItemStack(Material.DIAMOND, numDiamonds));
        p.getInventory().addItem(new ItemStack(Material.NETHER_STAR, clan.getBeacons().size()));

        clan.messageClan(ChatColor.YELLOW + clan.getName() + " was disbanded");
        ClansManager.getInstance().disbandClan(clan);
    }

    @Subcommand("invite")
    @Description("Invite a player to your clan")
    @Conditions("ownsclan")
    @CommandCompletion("@players")
    public static void inviteToClan(Player p, String otherPlayer) {
        Player other = Bukkit.getPlayer(otherPlayer);
        if(other == null) {
            p.sendRawMessage(ChatColor.RED + "Invalid player");
            return;
        }

        ECClan clan = ClansManager.getInstance().getClanByOwner(p);
        if(clan.isPlayerInClan(other)) {
            p.sendRawMessage(ChatColor.RED + "Player already in clan");
            return;
        }

        if(clan.isPlayerInvitee(other)) {
            p.sendRawMessage(ChatColor.RED + "Player already invited");
            return;
        }

        ClansManager.getInstance().invitePlayerToClan(clan, other);
        p.sendRawMessage(ChatColor.YELLOW + "Invitation sent");

        other.sendRawMessage(ChatColor.YELLOW + "You were invited to join " + clan.getName());
        TextComponent msg = new TextComponent(ChatColor.BLUE + "Click here to join or type /clan join <clan name>");
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + clan.getName()));
        other.sendMessage(msg);
    }

    @Subcommand("kick")
    @Description("Removes a player from your clan")
    @CommandCompletion("@players")
    @Conditions("ownsclan")
    public static void kickPlayer(Player p, String otherPlayer) {
        Player other = Bukkit.getPlayer(otherPlayer);
        if(other == null) {
            p.sendRawMessage(ChatColor.RED + "Invalid player");
            return;
        }

        if(other == p) {
            p.sendRawMessage(ChatColor.RED + "Cannot kick owner");
            return;
        }

        ECClan clan = ClansManager.getInstance().getClanByOwner(p);
        if(!clan.isPlayerInClan(other) && !clan.isPlayerInvitee(other)) {
            p.sendRawMessage(ChatColor.RED + "Player not in clan");
            return;
        }

        ClansManager.getInstance().kickMember(clan, other);
        clan.messageClan(ChatColor.RED + other.getName() + ChatColor.WHITE + " was kicked from " + ChatColor.YELLOW + clan.getName());
    }

    @Subcommand("join")
    @Description("Join a clan (once invited)")
    @CommandCompletion("@clan")
    public static void joinClan(Player p, String clanName) {
        ECClan clan = ClansManager.getInstance().getClanByName(clanName);
        if(clan == null) {
            p.sendRawMessage(ChatColor.RED + "Clan not found");
            return;
        }

        if(clan.isPlayerInClan(p)) {
            p.sendRawMessage(ChatColor.RED + "Already in clan");
            return;
        }

        if(!clan.isPlayerInvitee(p)) {
            p.sendRawMessage(ChatColor.RED + "You must be invited to the clan");
            return;
        }
        clan.messageClan(ChatColor.GREEN + p.getName() + ChatColor.WHITE + " Joined " + ChatColor.YELLOW + clan.getName());

        ClansManager.getInstance().acceptInvitation(clan, p);
        p.sendRawMessage(ChatColor.GREEN + "Clan joined");
    }

    private static boolean canPlayerTeleport(Player p) {
        boolean isMaxHealth = p.getHealth() >= p.getMaxHealth() - 0.499;
        if(!isMaxHealth) {
            p.sendActionBar(ChatColor.RED + "Must have full health to teleport");
            return false;
        }

        if(!p.isOnGround() && !p.isInWater()) {
            p.sendActionBar(ChatColor.RED + "Must be on ground or swimming to teleport");
            return false;
        }

        return true;
    }

    /*@Subcommand("home")
    @Conditions("ownsclan")
    @Description("Go to your clans home")
    public static void goMyClanHome(Player p) {
        ECClan clan = ClansManager.getInstance().getClanByOwner(p);

        if(clan.getHomeLocation() == null) {
            p.sendRawMessage(ChatColor.RED + "Clan home not set");
            return;
        }

        //Run checks
        if(!canPlayerTeleport(p)) {
            return;
        }

        p.sendActionBar(ChatColor.GREEN + "Going home");
        p.teleport(clan.getHomeLocation());
    }*/

    @Subcommand("home")
    @Description("Go to a clans home")
    @CommandCompletion("@clan")
    public static void goClanHome(Player p, String[] args) {
        ECClan clan = null;
        if(args.length == 0) {
            clan = ClansManager.getInstance().getClanByOwner(p);
            if(clan == null || clan.getHomeLocation() == null) {
                p.sendRawMessage(ChatColor.RED + "Clan home not set");
                return;
            }
        } else {
            clan = ClansManager.getInstance().getClanByName(String.join(" ", args));
        }

        if(clan == null) {
            p.sendRawMessage(ChatColor.RED + "Clan not found");
            return;
        }

        if(!clan.isPlayerInClan(p)) {
            p.sendRawMessage(ChatColor.RED + "You are not part of that clan");
            return;
        }

        if(clan.getHomeLocation() == null) {
            p.sendRawMessage(ChatColor.RED + "Clan home not set");
            return;
        }

        //Run checks
        if(!canPlayerTeleport(p)) {
            return;
        }

        p.sendActionBar(ChatColor.GREEN + "Going home");
        p.teleport(clan.getHomeLocation());
        getLogger().info("Teleported " + p.getName() + " to " + clan.getName());
    }



    @Subcommand("set")
    @Conditions("ownsclan")
    public class SetClanConfigCommand extends BaseCommand {
        @Subcommand("name")
        @Description("Set clan name")
        public void changeName(Player p, String name) {
            ECClan clan = ClansManager.getInstance().getClanByName(name);

            if(clan != null) {
                p.sendRawMessage(ChatColor.RED + "A clan with that name already exists");
                return;
            }

            ClansManager.getInstance().setClanName(ClansManager.getInstance().getClanByOwner(p), name);
            p.sendRawMessage(ChatColor.GREEN +  "Clan name changed to " + ChatColor.YELLOW + name);
        }

        @Subcommand("pvp")
        @CommandCompletion("@boolean")
        public void setPVP(Player p, boolean value) {
            ClansManager.getInstance().setClanSetting(ClansManager.getInstance().getClanByOwner(p), "pvp", value);
            p.sendRawMessage("Set PVP to " + value);
        }

        @Subcommand("pve")
        @CommandCompletion("@boolean")
        public void setPVE(Player p, boolean value) {
            ClansManager.getInstance().setClanSetting(ClansManager.getInstance().getClanByOwner(p), "pve", value);
            p.sendRawMessage("Set PVE to " + value);
        }

        @Subcommand("notify_entry")
        @CommandCompletion("@boolean")
        public void setNotifyEntry(Player p, boolean value) {
            ClansManager.getInstance().setClanSetting(ClansManager.getInstance().getClanByOwner(p), "msg", value);
            p.sendRawMessage("Set notify on entry to " + value);
        }

        @Subcommand("color")
        public void setColor(Player p, String hex) {
            if(hex.length() != 6) {
                p.sendRawMessage(ChatColor.RED + "Hex must be 6 characters");
                return;
            }

            Pattern pattern = Pattern.compile("^([A-Fa-f0-9]{6})$");
            boolean isValid = pattern.matcher(hex).find();
            if(!isValid) {
                p.sendRawMessage(ChatColor.RED + "Invalid hex string");
                return;
            }

            int col = Integer.parseInt(hex, 16);

            ECClan clan = ClansManager.getInstance().getClanByOwner(p);

            ClansManager.getInstance().setClanSetting(clan, "color", col);

            EduClan.regenMarkerSet();
            ClansManager.getInstance().regenerateDynmapMarkers();
        }

        @Subcommand("home")
        @Description("Set the home point of the clan")
        public void setHome(Player p) {
            Location l = p.getLocation();
            ECClan clan = ClansManager.getInstance().getClanByChunk(l.getChunk());

            if(clan != null && clan.getOwner().equals(p.getUniqueId())) {
                ClansManager.getInstance().setClanHome(clan, l);
                p.sendRawMessage(ChatColor.GREEN + "Clan home set");
            } else {
                p.sendRawMessage(ChatColor.RED + "You must be in your own land");
            }
        }
    }

    @Subcommand("buy")
    @Conditions("ownsclan")
    public class ClanBuyCommand extends BaseCommand {
        @Subcommand("chunk")
        @Description("Increase your chunk limit buy 1")
        public void buyChunk(Player p) {
            ECClan clan = ClansManager.getInstance().getClanByOwner(p);
            int chunksOverBase =  clan.getRawChunkLimit() - EduClan.config.getInt("startingChunkMax") + 1;
            int requiredDiamonds = calculateDiamondsRequired(chunksOverBase);
            boolean hasItems = p.getInventory().contains(Material.DIAMOND, requiredDiamonds);

            if(hasItems) {
                ItemStack req = new ItemStack(Material.DIAMOND, requiredDiamonds);
                p.getInventory().removeItemAnySlot(req);
                int newLimit = clan.getRawChunkLimit() + 1;
                ClansManager.getInstance().setChunkLimit(clan, newLimit);
                clan.messageClan(ChatColor.GREEN +  clan.getName() + " chunk limit increased to: " + (clan.getChunkLimit() + 1));
                return;
            } else {
                p.sendRawMessage(ChatColor.RED + "" + requiredDiamonds + " Diamonds Required");
            }
        }
    }

    @HelpCommand
    public static void onHelp(Player p, CommandHelp help) {
        help.showHelp();
    }

    private static int calculateDiamondsRequired(int chunks) {
        return Math.min((int)Math.floor(Math.pow(2.2, chunks/4.0)), 192);
    }
}
