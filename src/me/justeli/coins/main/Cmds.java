package me.justeli.coins.main;

import me.justeli.coins.item.CoinParticles;
import me.justeli.coins.settings.LoadSettings;
import me.justeli.coins.settings.Setting;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.justeli.coins.api.Complete;

class Cmds implements CommandExecutor {

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String l, String[] args)
	{
		if ( l.equalsIgnoreCase("coins") || l.equalsIgnoreCase("coin") ) {
            if (args.length >= 1) {

                switch (args[0]) {
                    case "reload":
                        if (sender.hasPermission("coins.admin"))
                        {
                            LoadSettings.remove();
                            LoadSettings.enums();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eConfig of &6Coins &ehas been reloaded."));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&oYou can check the loaded settings with &f&o/coins settings&e&o."));
                        }
                        else sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
                        break;
                    case "settings":
                        if (sender.hasPermission("coins.admin"))
                        {
                            String settings = LoadSettings.getSettings();
                            sender.sendMessage( ChatColor.translateAlternateColorCodes('&', settings) );
                        }
                        else sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
                        break;
                    case "drop":
                        if (sender.hasPermission("coins.drop"))
                            dropCoins(sender, args);
                        else
                            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
                        break;
                    default:
                        sendHelp(sender);
                        break;
                }


            } else sendHelp(sender);

            return true;
        }
		
		return false;
	}

	private void dropCoins (CommandSender sender, String[] args)
    {

        if (args.length >= 3)
        {
            Player p = Complete.onlinePlayer(args[1]);

            int amount = Integer.valueOf(args[2]);
            int radius = amount/20;
            if (radius < 2)
                radius = 2;
            if (args.length >= 4)
                radius = Integer.valueOf(args[3]);

            if (p == null)
            {
                sender.sendMessage(ChatColor.DARK_RED + "That player could not be found.");
                return;
            }

            for (String world : LoadSettings.hA.get(Setting._Array.disabledWorlds) )
                if (p.getWorld().getName().equalsIgnoreCase(world))
                {
                    sender.sendMessage(ChatColor.RED + "Coins are disabled in this world.");
                    return;
                }

            if (radius < 1 || radius > 50)
            {
                sender.sendMessage(ChatColor.DARK_RED + "That is an invalid radius.");
                return;
            }

            if (amount < 1 || amount > 500)
            {
                sender.sendMessage(ChatColor.DARK_RED + "That is an invalid amount.");
                return;
            }

            CoinParticles.dropCoins(p, radius, amount);
            sender.sendMessage(ChatColor.BLUE + "Spawned "+amount+" coins in radius "+radius+" around "+p.getName()+".");

        } else sender.sendMessage(ChatColor.RED + "Usage: /coins drop <player> <amount> [radius]");

    }

    private void sendHelp (CommandSender sender)
    {
        sender.sendMessage(ChatColor.DARK_RED + "                             * Help for Coins *");
        sender.sendMessage(ChatColor.RED + "/coins drop <player> <amount> [radius]" + ChatColor.GRAY + " - spawn coins");
        sender.sendMessage(ChatColor.RED + "/coins settings" + ChatColor.GRAY + " - list the currently loaded settings");
        sender.sendMessage(ChatColor.RED + "/coins reload" + ChatColor.GRAY + " - reload the settings from config.yml");
    }

}
