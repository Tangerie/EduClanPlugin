package xyz.tangerie.educlan;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.dynmap.DynmapAPI;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import xyz.tangerie.educlan.commands.*;
import xyz.tangerie.educlan.listeners.BlockInteractListener;
import xyz.tangerie.educlan.listeners.ChunkMoveListener;
import xyz.tangerie.educlan.listeners.EntityDamageListener;
import xyz.tangerie.educlan.listeners.PlayerSneakListener;
import xyz.tangerie.educlan.managers.ClansManager;
import xyz.tangerie.educlan.models.ECClan;

import java.util.stream.Collectors;

public final class EduClan extends JavaPlugin {

    public static MarkerAPI mapi = null;
    public static MarkerSet markerset = null;
    public static DynmapCommonAPI dynmapAPI = null;

    public static FileConfiguration config = null;

    private static EduClan eduPlugin = null;

    public static final String VERSION_ID = "1.4";

    @Override
    public void onEnable() {
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        Bukkit.getPluginManager().registerEvents(new ChunkMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSneakListener(), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        //manager.registerCommand(new TestCommand());
        manager.registerCommand(new ClanCommand());

        manager.getCommandCompletions().registerAsyncCompletion("clan", context -> {
            return ClansManager.getInstance().getClans().stream().map(x -> x.getName()).collect(Collectors.toList());
        });

        manager.getCommandCompletions().registerAsyncCompletion("boolean", context -> {
            return ImmutableList.of("true", "false");
        });

        manager.getCommandCompletions().registerAsyncCompletion("playerclans", context -> {
           return ClansManager.getInstance().getPlayerClans(context.getPlayer()).stream().map(x -> x.getName()).collect(Collectors.toList());
        });

        manager.getCommandConditions().addCondition("ownsclan", context -> {
           if(!context.getIssuer().isPlayer()) throw new ConditionFailedException("Must be player");

            ECClan clan = ClansManager.getInstance().getClanByOwner(context.getIssuer().getPlayer());
            if(clan == null) {
                throw new ConditionFailedException("Player must own a clan");
            }
        });

        manager.getCommandConditions().addCondition("!ownsclan", context -> {
            if(!context.getIssuer().isPlayer()) throw new ConditionFailedException("Must be player");

            ECClan clan = ClansManager.getInstance().getClanByOwner(context.getIssuer().getPlayer());
            if(clan != null) {
                throw new ConditionFailedException("Player already owns a clan");
            }
        });

        getServer().getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                getLogger().info(VERSION_ID + " EduClan Live");
                getLogger().info("Generating Markers");
                ClansManager.getInstance().regenerateDynmapMarkers();

                World world = Bukkit.getWorld("world");

                //Check all claims are valid
                for(ECClan clan : ClansManager.getInstance().getClans()) {
                    getLogger().info(clan.getName());
                    boolean valid = ClansManager.getInstance().areClanClaimsValid(clan);
                    getLogger().info("Valid: " + valid);
                    if(!valid) {
                        /*for(long c : clan.getChunks()) {
                            ClansManager.getInstance().unclaimChunk(world.getChunkAt(c), clan.getOwner());
                        }
                        ClansManager.getInstance().removeClanHome(clan);*/
                    } else {
                        if(clan.getHomeLocation() != null) {
                            ECClan otherClan = ClansManager.getInstance().getClanByChunk(clan.getHomeLocation().getChunk());
                            if(otherClan == null || !otherClan.getUuid().equals(clan.getUuid())) {
                                ClansManager.getInstance().removeClanHome(clan);
                            }
                        }
                    }
                }
            }
        }, 0L);

        eduPlugin = this;

        dynmapAPI = (DynmapCommonAPI) this.getServer().getPluginManager().getPlugin("dynmap");
        mapi = dynmapAPI.getMarkerAPI();
        if (mapi == null) {
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }

        markerset = mapi.createMarkerSet("educlan.markerset", "Clans", mapi.getMarkerIcons(), false);
        markerset.setHideByDefault(true);

        registerGodApple();
    }

    public static void regenMarkerSet() {
        markerset.deleteMarkerSet();
        markerset = mapi.createMarkerSet("educlan.markerset", "Clans", mapi.getMarkerIcons(), false);
    }

    public static EduClan getPlugin() {
        return eduPlugin;
    }

    @Override
    public void onDisable() {
        Bukkit.broadcastMessage("EduClan Shutting Down");
    }

    private void registerGodApple() {
        ItemStack apple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
        NamespacedKey key = new NamespacedKey(this, "god_apple");
        ShapedRecipe recipe = new ShapedRecipe(key, apple);
        recipe.shape("GGG", "GAG", "GGG");

        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('A', Material.APPLE);

        Bukkit.addRecipe(recipe);
    }
}
