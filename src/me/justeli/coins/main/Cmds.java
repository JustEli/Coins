package me.justeli.coins.main;

import me.justeli.coins.api.ActionBar;
import me.justeli.coins.item.CoinParticles;
import me.justeli.coins.settings.Settings;
import me.justeli.coins.settings.Config;
import net.md_5.bungee.api.ChatColor;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import me.justeli.coins.api.Complete;
import me.justeli.coins.item.Coin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

class Cmds implements CommandExecutor {

    public static final double[] DOUBLES = {};
    private static RegisteredServiceProvider<Economy> rep = Bukkit.getServicesManager().getRegistration(Economy.class);

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String l, String[] args)
	{
		if ( l.equalsIgnoreCase("coins") || l.equalsIgnoreCase("coin") ) {
            if (args.length >= 1) {

                switch (args[0]) {
                    case "reload":
                        if (sender.hasPermission("coins.admin"))
                        {
                            long ms = System.currentTimeMillis();
                            Settings.remove();
                            Settings.remove();
                            boolean success = Settings.enums();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&eConfig of &6Coins &ehas been reloaded in &a" + (System.currentTimeMillis() - ms) + "ms&e."));
                            if (!success) sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&c&oThere were some minor errors while reloading, check console."));
                            else sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&e&oYou can check the loaded settings with &f&o/coins settings&e&o."));
                        }
                        else sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
                        break;
                    case "settings":
                        if (sender.hasPermission("coins.admin"))
                        {
                            String settings = Settings.getSettings();
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
                    case "remove":
                        if (sender.hasPermission("coins.remove"))
                            removeCoins(sender, args);
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

        else if (l.equalsIgnoreCase("withdraw"))
        {
            if (!Settings.hB.get(Config.BOOLEAN.enableWithdraw))
                return false;

            if (!sender.hasPermission("coins.withdraw") || !(sender instanceof Player))
            {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have access to that command.");
                return true;
            }

            Player p = (Player) sender;

            if (args.length >= 1)
            {
                long amount;

                try { amount = Integer.valueOf(args[0]); }
                catch (NumberFormatException e)
                {
                    sender.sendMessage(ChatColor.DARK_RED + "That is an invalid amount of coins.");
                    return true;
                }

                if (amount > 0 && amount <= Settings.hD.get(Config.DOUBLE.maxWithdrawAmount) && rep.getProvider().getBalance(p) >= amount)
                {
                    p.getInventory().addItem( new Coin().withdraw(amount).item() );
                    rep.getProvider().withdrawPlayer(p, amount);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&e&oYou withdrawed &f&o$" + amount + " &e&oand received &f&o" + amount + " coins&e&o for it."));
                    new ActionBar("&4- &c$" + amount).send(p);
                }
                else p.sendMessage(ChatColor.RED + "You are not allowed to withdraw that much.");
            }

            else p.sendMessage(ChatColor.RED + "Usage: /withdraw <coins>");

        }
		
		return false;
	}

	private void dropCoins (CommandSender sender, String[] args)
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

            Location location;
            String name;
            if (p == null)
            {
                if (!args[1].contains(","))
                {
                    sender.sendMessage(ChatColor.DARK_RED + "That player could not be found.");
                    return;
                }
                else
                {
                    try
                    {
                        String[] coords = args[1].split(",");
                        double x = Double.valueOf(coords[0]);
                        double y = Double.valueOf(coords[1]);
                        double z = Double.valueOf(coords[2]);

                        location = new Location( sender instanceof Player ? ((Player) sender).getWorld() : Bukkit.getWorlds().get(0), x, y, z );
                        name = x + ", " + y + ", " + z;
                    }
                    catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
                    {
                        sender.sendMessage(ChatColor.DARK_RED + "One or more of those coords is an invalid number.");
                        return;
                    }
                }

            }
            else
            {
                location = p.getLocation();
                name = p.getName();
            }

            for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds) )
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

            CoinParticles.dropCoins(location, radius, amount);
            sender.sendMessage(ChatColor.BLUE + "Spawned "+amount+" coins in radius "+radius+" around " + name + ".");

        } else sender.sendMessage(ChatColor.RED + "Usage: /coins drop <player|x,y,z> <amount> [radius]");

    }

    private void removeCoins (CommandSender sender, String[] args)
    {

        double r = 0;
        List<Entity> mobs = Bukkit.getWorlds().get(0).getEntities();
        if (args.length >= 2 && sender instanceof Player)
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

        if (sender instanceof Player)
        {
            Player p = (Player) sender;
            mobs = p.getWorld().getEntities();
            if (r != 0) mobs = new ArrayList<>(p.getWorld().getNearbyEntities(p.getLocation(), r, r, r));
        }

        long amount = 0;
        for (Entity m : mobs)
        {
            if (m instanceof Item)
            {
                Item i = (Item) m;
                if (i.getItemStack().getItemMeta().getDisplayName() != null)
                    if (i.getItemStack().getItemMeta().getDisplayName().equals(
                            ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin) ) ))
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
        else if (sender instanceof Player)
            sender.sendMessage(ChatColor.BLUE + "Removed " + amount + " coins in this world.");
        else
            sender.sendMessage(ChatColor.BLUE + "Removed " + amount + " coins in the default world.");
    }

    private void sendHelp (CommandSender sender)
    {
        if (sender instanceof Player) sender.sendMessage(ChatColor.DARK_RED + "                             * Help for Coins *");
        else sender.sendMessage(ChatColor.DARK_RED + "* Help for Coins *");

        if (sender.hasPermission("coins.drop"))
            sender.sendMessage(ChatColor.RED + "/coins drop <player|x,y,z> <amount> [radius]" + ChatColor.GRAY + " - spawn coins");

        if (sender.hasPermission("coins.remove"))
            sender.sendMessage(ChatColor.RED + "/coins remove [radius|all]" + ChatColor.GRAY + " - remove coins in a radius");

        if (sender.hasPermission("coins.admin"))
        {
            sender.sendMessage(ChatColor.RED + "/coins settings" + ChatColor.GRAY + " - list the currently loaded settings");
            sender.sendMessage(ChatColor.RED + "/coins reload" + ChatColor.GRAY + " - reload the settings from config.yml");
        }

        if (Settings.hB.get(Config.BOOLEAN.enableWithdraw) && sender.hasPermission("coins.withdraw"))
            sender.sendMessage(ChatColor.RED + "/withdraw <coins>" + ChatColor.GRAY + " - withdraw some money into coins");
    }

}
