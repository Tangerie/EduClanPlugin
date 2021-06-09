package xyz.tangerie.educlan.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import xyz.tangerie.educlan.managers.*;
import xyz.tangerie.educlan.models.*;
import xyz.tangerie.edulib.shop.InventoryHelper;

import java.util.*;

@CommandAlias("clan")
public class ClanCommand extends BaseCommand {

    @Subcommand("list")
    @Description("List all clans")
    public static void listClans(Player p) {
        p.sendRawMessage(String.format("---- All [%d] Clans ----", ClansManager.getInstance().getClans().size()));
        for(ECClan c : ClansManager.getInstance().getClans()) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(c.getOwner());
            p.sendRawMessage(ChatColor.YELLOW + c.getName() + ChatColor.BLUE + " [" + owner.getName() + "]");
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
            p.sendRawMessage(ChatColor.YELLOW + c.getName() + ChatColor.BLUE + " [" + owner.getName() + "]");
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
        ECClan clan = ClansManager.getInstance().getClanByChunk(p.getChunk());
        if(clan != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(clan.getOwner());
            p.sendRawMessage("You cannot claim this land as it belongs to " + ChatColor.YELLOW + clan.getName() + ChatColor.BLUE + " [" + owner.getName() + "]");
            return;
        }

        //Claim chunk
        ClansManager.getInstance().claimChunk(p.getChunk(), p);
        p.sendRawMessage(ChatColor.GREEN + "Claimed Chunk");
    }

    @Subcommand("unclaim")
    @Description("Unclaim the current chunk")
    @Conditions("ownsclan")
    public static void unclaimChunk(Player p) {
        ECClan clan = ClansManager.getInstance().getClanByChunk(p.getChunk());
        if(clan == null || !clan.getOwner().equals(p.getUniqueId())) {
            p.sendRawMessage(ChatColor.RED + "You do not own this chunk");
            return;
        }

        ClansManager.getInstance().unclaimChunk(p.getChunk(), p);
        p.sendRawMessage(ChatColor.GREEN + "Unclaimed Chunk");
    }

    @Subcommand("disband")
    @Description("Disband your clan")
    @Conditions("ownsclan")
    public static void disbandClan(Player p) {
        ECClan clan = ClansManager.getInstance().getClanByOwner(p);

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
        clan.messageClan(ChatColor.RED + p.getName() + ChatColor.WHITE + " was kicked from " + ChatColor.YELLOW + clan.getName());
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
    }

    @Subcommand("gui")
    public static void showShop(Player p) {
        //p.openInventory(i);
        p.openInventory(ClansManager.getClanManager(p));
    }

    @HelpCommand
    public static void onHelp(Player p, CommandHelp help) {
        help.showHelp();
    }
}
