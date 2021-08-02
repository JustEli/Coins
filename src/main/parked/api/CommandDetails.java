package me.justeli.coins.command.api;

import org.bukkit.command.CommandSender;

/**
 * Created by Eli on July 18, 2021.
 * Coins: me.justeli.coins.command.api
 */
public class CommandDetails
{
    private final CommandSender sender;
    private final String arguments;

    public CommandDetails (CommandSender sender, String arguments)
    {
        this.sender = sender;
        this.arguments = arguments;
    }

    public <T extends CommandSender> T sender ()
    {
        return (T) sender;
    }

    public <T> T argument (String name)
    {
        return (T) "";
    }

    public <T> T argument (int number)
    {
        return (T) "";
    }
}
