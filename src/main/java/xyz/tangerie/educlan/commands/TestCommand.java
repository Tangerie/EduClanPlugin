package xyz.tangerie.educlan.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import xyz.tangerie.educlan.managers.ClansManager;

@CommandAlias("test")
public class TestCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    @Description("A test command")
    public static void onTest(Player player) {
        player.sendRawMessage("Test Message");
        player.sendRawMessage(String.valueOf(ClansManager.getInstance().getClans().size()));
    }

    @Default
    @Subcommand("help")
    public static void onTestOverload(Player player, String a) {
        player.sendRawMessage(a);
    }
}
