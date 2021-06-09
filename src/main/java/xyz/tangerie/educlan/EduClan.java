package xyz.tangerie.educlan;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.tangerie.educlan.commands.*;
import xyz.tangerie.educlan.listeners.BlockInteractListener;
import xyz.tangerie.educlan.listeners.ChunkMoveListener;
import xyz.tangerie.educlan.listeners.EntityDamageListener;
import xyz.tangerie.educlan.managers.ClansManager;
import xyz.tangerie.educlan.models.ECClan;

import java.util.stream.Collectors;

public final class EduClan extends JavaPlugin {
    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        Bukkit.getPluginManager().registerEvents(new ChunkMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new TestCommand());
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

        getLogger().info("EduClan Live");
    }

    @Override
    public void onDisable() {
        Bukkit.broadcastMessage("EduClan Shutting Down");
    }
}
