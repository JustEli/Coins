package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Message;
import me.justeli.coins.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** by Eli on August 02, 2021 **/
public final class CoinsDisabled
        implements CommandExecutor
{
    private final Coins coins;

    public CoinsDisabled (Coins coins)
    {
        this.coins = coins;
    }

    @Override
    public boolean onCommand (
            @NotNull CommandSender sender,
            @NotNull Command command,
            String label,
            @NotNull String[] args)
    {
        if (!label.equalsIgnoreCase("coins") && !label.equalsIgnoreCase("coin") && !label.equalsIgnoreCase("withdraw"))
            return false;

        sender.sendMessage(Message.DISABLED_REASONS.toString());
        for (String message : this.coins.disabledReasons())
        {
            sender.sendMessage(Util.color(" - &c" + message));
        }

        return true;
    }
}