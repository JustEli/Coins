package me.justeli.coins.main;

import me.justeli.coins.Coins;
import me.justeli.coins.api.ActionBar;
import me.justeli.coins.api.Complete;
import me.justeli.coins.api.Extras;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Messages;
import me.justeli.coins.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Cmds
        implements CommandExecutor
{
    private static String color (String message)
    {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public boolean onCommand (CommandSender sender, Command cmd, String l, String[] args)
    {
        if (l.equalsIgnoreCase("coins") || l.equalsIgnoreCase("coin"))
        {
            if (args.length >= 1)
            {
                switch (args[0])
                {
                    case "reload":
                        if (sender.hasPermission("coins.admin"))
                        {
                            long ms = System.currentTimeMillis();
                            Settings.remove();
                            Settings.remove();
                            Extras.resetMultiplier();
                            boolean success = Settings.enums();
                            sender.sendMessage(color(Messages.RELOAD_SUCCESS.toString()
                                    .replace("{0}", Long.toString(System.currentTimeMillis() - ms))));
                            if (!success)
                                sender.sendMessage(color(Messages.MINOR_ISSUES.toString()));
                            else
                                sender.sendMessage(color(Messages.CHECK_SETTINGS.toString()));
                        }
                        else
                            noPerm(sender);
                        break;
                    case "settings":
                        if (sender.hasPermission("coins.admin"))
                        {
                            String settings = Settings.getSettings();
                            sender.sendMessage(color(settings));
                        }
                        else
                            noPerm(sender);
                        break;
                    case "drop":
                        if (sender.hasPermission("coins.drop"))
                            dropCoins(sender, args);
                        else
                            noPerm(sender);
                        break;
                    case "remove":
                        if (sender.hasPermission("coins.remove"))
                            removeCoins(sender, args);
                        else
                            noPerm(sender);
                        break;
                    case "lang":
                        for (Messages m : Messages.values())
                            sender.sendMessage(m.toString());
                        break;
                    case "version":
                    case "update":
                        if (sender.hasPermission("coins.admin"))
                        {
                            String version = Coins.getUpdate();
                            String current = Coins.getInstance().getDescription().getVersion();
                            sender.sendMessage(color("&eVersion currently installed: &f" + current));
                            sender.sendMessage(color("&eLatest released version: &f" + version));
                            if (version.equals(current))
                                sender.sendMessage(color("&aYou're up to date with version " + current + "."));
                            else
                            {
                                sender.sendMessage(color("&cConsider updating the plugin to version " + version + "!"));
                                sender.sendMessage("https://www.spigotmc.org/resources/coins.33382/");
                            }
                        }
                        break;
                    default:
                        sendHelp(sender);
                        break;
                }


            }
            else
                sendHelp(sender);

            return true;
        }

        else if (l.equalsIgnoreCase("withdraw"))
        {
            if (!Settings.hB.get(Config.BOOLEAN.enableWithdraw))
                return false;

            if (!sender.hasPermission("coins.withdraw") || !(sender instanceof Player))
            {
                noPerm(sender);
                return true;
            }

            Player player = (Player) sender;
            if (Settings.hA.get(Config.ARRAY.disabledWorlds).contains(player.getWorld().getName()))
            {
                sender.sendMessage(color(Messages.COINS_DISABLED.toString()));
                return true;
            }

            if (player.getInventory().firstEmpty() == -1)
            {
                player.sendMessage(color(Messages.INVENTORY_FULL.toString()));
                return true;
            }

            if (args.length == 0)
            {
                player.sendMessage(color(Messages.WITHDRAW_USAGE.toString()));
                return true;
            }

            int worth = parseInt(args[0]);
            int amount = args.length >= 2? parseInt(args[1]) : 1;
            int total = worth * amount;

            if (worth < 1 || amount < 1 || total < 1 || amount > 64)
            {
                sender.sendMessage(color(Messages.INVALID_AMOUNT.toString()));
                return true;
            }

            if (worth <= Settings.hD.get(Config.DOUBLE.maxWithdrawAmount) && Coins.getEconomy().getBalance(player) >= total)
            {
                ItemStack coin = new Coin().withdraw(worth).item();
                coin.setAmount(amount);

                player.getInventory().addItem(coin);
                Coins.getEconomy().withdrawPlayer(player, total);

                player.sendMessage(color(Messages.WITHDRAW_COINS.toString().replace("{0}", Long.toString(total))));

                new ActionBar(Settings.hS.get(Config.STRING.deathMessage).replace("%amount%", String.valueOf(total))
                        .replace("{$}", Settings.hS.get(Config.STRING.currencySymbol))).send(player);
            }
            else
            {
                player.sendMessage(color(Messages.NOT_THAT_MUCH.toString()));
            }
        }
        return false;
    }

    private int parseInt (String arg)
    {
        try { return Integer.parseInt(arg); }
        catch (NumberFormatException e) { return 0; }
    }

    private void dropCoins (CommandSender sender, String[] args)
    {
        if (args.length >= 3)
        {
            Player p = Complete.onlinePlayer(args[1]);

            int amount;
            try {amount = Integer.parseInt(args[2]); }
            catch (NumberFormatException e)
            {
                sender.sendMessage(color(Messages.INVALID_NUMBER.toString()));
                return;
            }

            int radius = amount / 20;
            if (radius < 2)
                radius = 2;

            if (args.length >= 4)
            {
                try {radius = Integer.parseInt(args[3]);}
                catch (NumberFormatException e)
                {
                    sender.sendMessage(color(Messages.INVALID_NUMBER.toString()));
                    return;
                }
            }

            Location location;
            String name;
            if (p == null)
            {
                if (!args[1].contains(","))
                {
                    sender.sendMessage(color(Messages.PLAYER_NOT_FOUND.toString()));
                    return;
                }
                else
                {
                    try
                    {
                        String[] coords = args[1].split(",");
                        double x = Double.parseDouble(coords[0]);
                        double y = Double.parseDouble(coords[1]);
                        double z = Double.parseDouble(coords[2]);

                        location = new Location(coords.length == 4? Bukkit.getWorld(coords[3]) : (sender instanceof Player? ((Player) sender)
                                .getWorld() : Bukkit.getWorlds().get(0)), x, y, z);
                        name = x + ", " + y + ", " + z;
                    }
                    catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e)
                    {
                        sender.sendMessage(color(Messages.COORDS_NOT_FOUND.toString()));
                        return;
                    }
                }

            }
            else
            {
                location = p.getLocation();
                name = p.getName();
            }

            if (p != null || sender instanceof Player)
            {
                if (p == null)
                    p = (Player) sender;

                for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds))
                {
                    if (p.getWorld().getName().equalsIgnoreCase(world))
                    {
                        sender.sendMessage(color(Messages.COINS_DISABLED.toString()));
                        return;
                    }
                }
            }

            if (radius < 1 || radius > 80)
            {
                sender.sendMessage(color(Messages.INVALID_RADIUS.toString()));
                return;
            }

            if (amount < 1 || amount > 1000)
            {
                sender.sendMessage(color(Messages.INVALID_AMOUNT.toString()));
                return;
            }

            Coins.particles(location, radius, amount);
            sender.sendMessage(color(Messages.SPAWNED_COINS.toString()).replace("{0}", Long.toString(amount)).replace("{1}", Long.toString(radius))
                    .replace("{2}", name));

        }
        else
            sender.sendMessage(color(Messages.DROP_USAGE.toString()));

    }

    private void removeCoins (CommandSender sender, String[] args)
    {

        double r = 0;
        List<Entity> mobs = Bukkit.getWorlds().get(0).getEntities();
        if (args.length >= 2 && sender instanceof Player)
        {
            if (!args[1].equalsIgnoreCase("all"))
            {
                try {r = Integer.parseInt(args[1]);}
                catch (NumberFormatException e)
                {
                    sender.sendMessage(color(Messages.INVALID_RADIUS.toString()));
                    return;
                }
                if (r < 1 || r > 80)
                {
                    sender.sendMessage(color(Messages.INVALID_RADIUS.toString()));
                    return;
                }
            }

        }

        if (sender instanceof Player)
        {
            Player p = (Player) sender;
            mobs = p.getWorld().getEntities();
            if (r != 0)
                mobs = new ArrayList<>(p.getWorld().getNearbyEntities(p.getLocation(), r, r, r));
        }

        long amount = 0;
        for (Entity m : mobs)
        {
            if (m instanceof Item)
            {
                Item i = (Item) m;
                if (i.getItemStack().getItemMeta() != null && i.getItemStack().getItemMeta().hasDisplayName())
                {
                    if (i.getItemStack().getItemMeta().getDisplayName().equals((color(Settings.hS.get(Config.STRING.nameOfCoin)))))
                    {
                        amount++;
                        double random = (Math.random() * 3);
                        long rand = (long) random * 5;
                        i.setVelocity(new Vector(0, random, 0));
                        new BukkitRunnable()
                        {
                            int a = 0;

                            public void run ()
                            {
                                a += 1;
                                if (a >= 1)
                                {
                                    i.remove();
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Coins.getInstance(), rand, rand);
                    }
                }
            }
        }
        sender.sendMessage(color(Messages.REMOVED_COINS.toString().replace("{0}", Long.toString(amount))));
    }

    private void sendHelp (CommandSender sender)
    {
        String version = Coins.getInstance().getDescription().getVersion();
        String update = Coins.getUpdate();
        String notice = "";
        if (!update.equals(version))
            notice = " (outdated; /coins update)";

        sender.sendMessage(color(Messages.COINS_HELP.toString() + " " + version + notice));

        if (sender.hasPermission("coins.drop"))
            sender.sendMessage(color(Messages.DROP_USAGE.toString()));

        if (sender.hasPermission("coins.remove"))
            sender.sendMessage(color(Messages.REMOVE_USAGE.toString()));

        if (sender.hasPermission("coins.admin"))
        {
            sender.sendMessage(color(Messages.SETTINGS_USAGE.toString()));
            sender.sendMessage(color(Messages.RELOAD_USAGE.toString()));
            sender.sendMessage(color(Messages.VERSION_CHECK.toString()));
        }

        if (Settings.hB.get(Config.BOOLEAN.enableWithdraw) && sender.hasPermission("coins.withdraw"))
            sender.sendMessage(color(Messages.WITHDRAW_USAGE.toString()));
    }

    private void noPerm (CommandSender sender)
    {
        sender.sendMessage(color(Messages.NO_PERMISSION.toString()));
    }
}
