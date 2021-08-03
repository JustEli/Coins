package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Message;
import me.justeli.coins.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** by Eli at August 02, 2021 **/
public class CoinsDisabled
        implements CommandExecutor
{
    @Override
    public boolean onCommand (
            @NotNull CommandSender sender,
            @NotNull Command command,
            String label,
            @NotNull String[] args)
    {
        if (!label.equalsIgnoreCase("coins") && !label.equalsIgnoreCase("coin") && !label.equalsIgnoreCase("withdraw"))
            return false;

        sender.sendMessage(Util.color(Message.DISABLED_REASONS.toString()));
        for (String message : Coins.getDisabledReasons())
        {
            sender.sendMessage(Util.color(" - &c" + message));
        }

        return true;
    }
}