package me.justeli.coins.command.api;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Eli on July 18, 2021.
 * Coins: me.justeli.coins.command.api
 */
public class Command
{
    private final String command;
    private final List<String> aliases = new ArrayList<>();
    private final List<CommandArgument> arguments = new ArrayList<>();

    private String description = "";
    private String permission = "";

    private Command (String command, String... aliases)
    {
        this.command = command;
        Collections.addAll(this.aliases, aliases);
    }

    public static Command of (String command, String... aliases)
    {
        return new Command(command, aliases);
    }

    public CommandArgument argument ()
    {
        CommandArgument argument = new CommandArgument(this);

        arguments.add(argument);
        return argument;
    }

    public CommandArgument argument (Argument... args)
    {
        CommandArgument argument = new CommandArgument(this, args);

        arguments.add(argument);
        return argument;
    }

    public CommandArgument argument (String... args)
    {
        CommandArgument argument = new CommandArgument(this, args);

        arguments.add(argument);
        return argument;
    }

    public void help ()
    {

    }

    public void register (JavaPlugin plugin)
    {
        try
        {
            final Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());


            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand command = constructor.newInstance(name, );

            command.setExecutor(CommandSender);
            command.setName(this.command);
            command.setAliases(this.aliases);
            command.setDescription(this.description);
            command.setUsage("Correct usage: /");
            command.setPermission(this.permission);
            command.setPermissionMessage("no access");

            commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void unregister ()
    {

    }


    private static class Executor
            implements CommandExecutor, TabCompleter
    {
        private final Method method;

        private Executor (Method method, Commander instance)
        {
            this.method = method;
        }

        @Override
        public boolean onCommand (CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
        {
            if (!order.canAccess(sender))
            {
                String error = ChatColor.RED + "Error: " + ChatColor.RESET;

                if (order.consoleOnly)
                {
                    sender.sendMessage(error + "Only console may execute this command.");
                }
                if (order.playersOnly)
                {
                    sender.sendMessage(error + "Only players may execute this command.");
                }
                return true;
            }

            CommandDetails details = new Details(sender, label, args);
            Object[] parameters = order.getParameters(details);

            try
            {
                return (boolean) method.invoke(instance, parameters);
            }
            catch (Exception ex)
            {
                Bukkit.getPluginManager().callEvent(new CommandErrorEvent(sender, label, args, ex));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete (@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, String alias, @NotNull String[] args)
        {
            return null;
        }
    }
}
