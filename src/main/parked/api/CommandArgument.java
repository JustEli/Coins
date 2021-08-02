package me.justeli.coins.command.api;

import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

/**
 * Created by Eli on July 18, 2021.
 * Coins: me.justeli.coins.command.api
 */
public class CommandArgument
{
    private final Command command;

    CommandArgument (Command command, Argument[] arguments)
    {
        this.command = command;
    }

    CommandArgument (Command command, String... arguments)
    {
        this.command = command;
    }

    public void queue (Consumer<CommandDetails> runnable)
    {

    }

    public void complete (Consumer<CommandDetails> runnable)
    {

    }

    public CommandArgument permission (String permission)
    {
        return this;
    }

    public CommandArgument description (String description)
    {
        return this;
    }

    public CommandArgument argument (Object... arguments)
    {
        return this;
    }

    public CommandArgument require (Class<? extends CommandSender> klass)
    {
        return this;
    }
}
