package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Message;
import me.justeli.coins.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/* Eli @ August 2, 2021 (creation) */
public final class DisabledCommand
    implements CommandExecutor
{
    private final Coins coins;
    private final Set<PluginCommand> commands = new HashSet<>();

    public DisabledCommand (Coins coins)
    {
        this.coins = coins;
        this.commands.add(coins.getCommand("coins"));
        this.commands.add(coins.getCommand("withdraw"));
    }

    public Set<PluginCommand> commands ()
    {
        return commands;
    }

    @Override
    public boolean onCommand (@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        sender.sendMessage(Message.DISABLED_REASONS.toString());
        for (String message : this.coins.disabledReasons())
        {
            sender.sendMessage(Util.color("- &c" + message));
        }
        return true;
    }
}
