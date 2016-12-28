package me.justeli.coins.main;

import me.justeli.coins.item.CoinParticles;
import me.justeli.coins.settings.LoadSettings;
import me.justeli.coins.settings.Setting;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import me.justeli.coins.api.Complete;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

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
                            dropCoins((Player)sender, args);
                        else
                            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
                        break;
                    case "remove":
                        if (sender.hasPermission("coins.remove"))
                            removeCoins((Player) sender, args);
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

	private void dropCoins (Player sender, String[] args)
    {

        if (args.length >= 3)
        {
            Player p = Complete.onlinePlayer(args[1]);

            int amount;
            try {amount = Integer.valueOf(args[2]); }
            catch (NumberFormatException e)
            {
                sender.sendMessage(ChatColor.DARK_RED + "That is an invalid number.");
                return;
            }

            int radius = amount/20;
            if (radius < 2)
                radius = 2;
            if (args.length >= 4)

            {
                try {radius = Integer.valueOf(args[3]);}
                catch (NumberFormatException e)
                {
                    sender.sendMessage(ChatColor.DARK_RED + "That is an invalid number.");
                    return;
                }
            }


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

            if (radius < 1 || radius > 80)
            {
                sender.sendMessage(ChatColor.DARK_RED + "That is an invalid radius.");
                return;
            }

            if (amount < 1 || amount > 1000)
            {
                sender.sendMessage(ChatColor.DARK_RED + "That is an invalid amount.");
                return;
            }

            CoinParticles.dropCoins(p, radius, amount);
            sender.sendMessage(ChatColor.BLUE + "Spawned "+amount+" coins in radius "+radius+" around "+p.getName()+".");

        } else sender.sendMessage(ChatColor.RED + "Usage: /coins drop <player> <amount> [radius]");

    }

    private void removeCoins (Player sender, String[] args)
    {
        double r = 0;
        if (args.length >= 2)
        {
            if (!args[1].equalsIgnoreCase("all"))
            {
                try {r = Integer.valueOf(args[1]);}
                catch (NumberFormatException e) { sender.sendMessage(ChatColor.DARK_RED + "That is an invalid number."); return; }
                if (r < 1 || r > 80)
                {
                    sender.sendMessage(ChatColor.DARK_RED + "That is an invalid radius.");
                    return;
                }
            }
        }

        List<Entity> mobs = sender.getWorld().getEntities();
        if (r != 0) mobs = new ArrayList<>(sender.getWorld().getNearbyEntities(sender.getLocation(), r, r, r));
        long amount = 0;
        for (Entity m : mobs)
        {
            if (m instanceof Item)
            {
                Item i = (Item) m;
                if (i.getItemStack().getItemMeta().getDisplayName() != null)
                    if (i.getItemStack().getItemMeta().getDisplayName().equals(
                            ChatColor.translateAlternateColorCodes('&', LoadSettings.hS.get(Setting._String.nameOfCoin) ) ))
                    {
                        amount ++;
                        double random = (Math.random()*3);
                        long rand = (long) random * 5;
                        i.setVelocity(new Vector(0, random, 0));
                        new BukkitRunnable() {
                            int a = 0;
                            public void run() {
                                a += 1;
                                if (a >= 1) {
                                    i.remove();
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Load.main, rand, rand);
                    }
            }

        }
        if (r != 0)
            sender.sendMessage(ChatColor.BLUE + "Removed " + amount + " coins in a radius of " + r + ".");
        else
            sender.sendMessage(ChatColor.BLUE + "Removed " + amount + " coins in this world.");
    }

    private void sendHelp (CommandSender sender)
    {
        sender.sendMessage(ChatColor.DARK_RED + "                             * Help for Coins *");
        sender.sendMessage(ChatColor.RED + "/coins drop <player> <amount> [radius]" + ChatColor.GRAY + " - spawn coins");
        sender.sendMessage(ChatColor.RED + "/coins remove [radius|all]" + ChatColor.GRAY + " - remove coins in a radius");
        sender.sendMessage(ChatColor.RED + "/coins settings" + ChatColor.GRAY + " - list the currently loaded settings");
        sender.sendMessage(ChatColor.RED + "/coins reload" + ChatColor.GRAY + " - reload the settings from config.yml");
    }

}
