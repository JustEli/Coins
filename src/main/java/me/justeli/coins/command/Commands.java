package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.util.ActionBar;
import me.justeli.coins.util.Util;
import me.justeli.coins.config.Config;
import me.justeli.coins.config.Message;
import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class Commands
        implements CommandExecutor
{
    private final Coins coins;

    public Commands (Coins coins)
    {
        this.coins = coins;
    }

    private final static SplittableRandom RANDOM = new SplittableRandom();

    private static String color (String message)
    {
        return Util.color(message);
    }

    @Override
    public boolean onCommand (@NotNull CommandSender sender, @NotNull Command cmd, String l, @NotNull String[] args)
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

                            this.coins.reload();
                            int warnings = this.coins.settings().getWarningCount();

                            sender.sendMessage(Message.RELOAD_SUCCESS.toString().replace("{0}", Long.toString(System.currentTimeMillis() - ms)));
                            if (warnings != 0)
                            {
                                sender.sendMessage(Message.MINOR_ISSUES.toString());
                            }
                            else
                            {
                                sender.sendMessage(Message.CHECK_SETTINGS.toString());
                            }
                        }
                        else
                            noPerm(sender);
                        break;
                    case "settings":
                        if (sender.hasPermission("coins.admin"))
                        {
                            for (String setting : this.coins.settings().getReadableConfig())
                                sender.sendMessage(color(setting));
                        }
                        else
                        {
                            noPerm(sender);
                        }
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
                    case "language":
                        for (Message message : Message.values())
                        {
                            sender.sendMessage(message.toString());
                        }
                        break;
                    case "version":
                    case "update":
                        if (sender.hasPermission("coins.admin"))
                        {
                            String version = this.coins.latest();
                            String current = this.coins.getDescription().getVersion();
                            sender.sendMessage(Message.CURRENTLY_INSTALLED.replace(current));
                            sender.sendMessage(Message.LATEST_VERSION.replace(version));
                            if (version.equals(current))
                            {
                                sender.sendMessage(Message.UP_TO_DATE.replace(current));
                            }
                            else if (version.equals("Unknown"))
                            {
                                sender.sendMessage(Message.LATEST_RETRIEVE_FAIL.toString());
                            }
                            else
                            {
                                sender.sendMessage(Message.CONSIDER_UPDATING.replace(version));
                                sender.sendMessage(color("&9https://www.spigotmc.org/resources/coins.33382/"));
                            }
                        }
                        else
                        {
                            noPerm(sender);
                        }
                        break;
                    case "toggle":
                        if (sender.hasPermission("coins.toggle"))
                        {
                            Message abled = this.coins.toggleDisabled()? Message.ENABLED : Message.DISABLED;
                            sender.sendMessage(Message.GLOBALLY_DISABLED_INFORM.replace(abled));
                            if (this.coins.isDisabled())
                            {
                                sender.sendMessage(Message.DISABLED_DESCRIPTION.toString());
                            }
                        }
                        else
                        {
                            noPerm(sender);
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
            if (this.coins.isDisabled())
            {
                sender.sendMessage(Message.COINS_DISABLED.toString());
                return true;
            }

            if (!Config.ENABLE_WITHDRAW)
            {
                sender.sendMessage(Message.WITHDRAWING_DISABLED.toString());
                return false;
            }

            if (!sender.hasPermission("coins.withdraw") || !(sender instanceof Player))
            {
                noPerm(sender);
                return true;
            }

            Player player = (Player) sender;
            if (Util.isDisabledHere(player.getWorld()))
            {
                sender.sendMessage(Message.COINS_DISABLED.toString());
                return true;
            }

            if (player.getInventory().firstEmpty() == -1)
            {
                player.sendMessage(Message.INVENTORY_FULL.toString());
                return true;
            }

            if (args.length == 0)
            {
                player.sendMessage(Message.WITHDRAW_USAGE.toString());
                return true;
            }

            double worth = parseDouble(args[0]);
            int amount = args.length >= 2? parseInt(args[1]) : 1;
            double total = worth * amount;

            if (worth < 1 || amount < 1 || total < 1 || amount > 64)
            {
                sender.sendMessage(Message.INVALID_AMOUNT.toString());
                return true;
            }

            if (worth <= Config.MAX_WITHDRAW_AMOUNT)
            {
                this.coins.economy().canAfford(player.getUniqueId(), total, has ->
                {
                    if (has)
                    {
                        // todo needs testing
                        this.coins.economy().withdraw(player.getUniqueId(), total, () ->
                        {
                            ItemStack coin = this.coins.getCreateCoin().withdrawn(worth);
                            coin.setAmount(amount);

                            player.getInventory().addItem(coin);

                            player.sendMessage(Message.WITHDRAW_COINS.replace(Util.doubleToString(total)));
                            ActionBar.of(Util.formatAmountAndCurrency(Config.DEATH_MESSAGE, total)).send(player);
                        });
                    }
                    else
                    {
                        player.sendMessage(Message.NOT_THAT_MUCH.toString());
                    }
                });

            }
            else
            {
                player.sendMessage(Message.NOT_THAT_MUCH.toString());
            }
        }
        return false;
    }

    private int parseInt (String arg)
    {
        try { return Integer.parseInt(arg); }
        catch (NumberFormatException e) { return 0; }
    }

    private double parseDouble (String arg)
    {
        try { return Util.round(new Double(arg)); }
        catch (NumberFormatException e) { return 0; }
    }

    private void dropCoins (CommandSender sender, String[] args)
    {
        if (args.length >= 3)
        {
            Player p = Util.getOnlinePlayer(args[1]);

            int amount;
            try {amount = Integer.parseInt(args[2]); }
            catch (NumberFormatException e)
            {
                sender.sendMessage(Message.INVALID_NUMBER.toString());
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
                    sender.sendMessage(Message.INVALID_NUMBER.toString());
                    return;
                }
            }

            Location location;
            String name;
            if (p == null)
            {
                if (!args[1].contains(","))
                {
                    sender.sendMessage(Message.PLAYER_NOT_FOUND.toString());
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
                        sender.sendMessage(Message.COORDS_NOT_FOUND.toString());
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

                for (String world : Config.DISABLED_WORLDS)
                {
                    if (p.getWorld().getName().equalsIgnoreCase(world))
                    {
                        sender.sendMessage(Message.COINS_DISABLED.toString());
                        return;
                    }
                }
            }

            if (radius < 1 || radius > 80)
            {
                sender.sendMessage(Message.INVALID_RADIUS.toString());
                return;
            }

            if (amount < 1 || amount > 1000)
            {
                sender.sendMessage(Message.INVALID_AMOUNT.toString());
                return;
            }

            dropCoins(location, radius, amount);
            sender.sendMessage(
                    Message.SPAWNED_COINS.replace(
                                    Long.toString(amount),
                                    Long.toString(radius),
                                    name
                    )
            );

        }
        else
        {
            sender.sendMessage(Message.DROP_USAGE.toString());
        }
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
                    sender.sendMessage(Message.INVALID_RADIUS.toString());
                    return;
                }
                if (r < 1 || r > 80)
                {
                    sender.sendMessage(Message.INVALID_RADIUS.toString());
                    return;
                }
            }

        }

        if (sender instanceof Player)
        {
            Player p = (Player) sender;
            mobs = p.getWorld().getEntities();
            if (r != 0)
            {
                mobs = new ArrayList<>(p.getWorld().getNearbyEntities(p.getLocation(), r, r, r));
            }
        }

        long amount = 0;
        for (Entity m : mobs)
        {
            if (m instanceof Item)
            {
                Item i = (Item) m;
                if (i.getItemStack().getItemMeta() != null && i.getItemStack().getItemMeta().hasDisplayName())
                {
                    if (this.coins.getCoinUtil().isCoin(i.getItemStack()))
                    {
                        amount++;
                        double random = (RANDOM.nextDouble() * 3);
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
                        }.runTaskTimer(this.coins, rand, rand);
                    }
                }
            }
        }
        sender.sendMessage(Message.REMOVED_COINS.replace(Long.toString(amount)));
    }

    private void sendHelp (CommandSender sender)
    {
        String version = this.coins.getDescription().getVersion();
        String update = this.coins.latest();
        String notice = "";
        if (this.coins.isDisabled())
        {
            notice = " " + Message.GLOBALLY_DISABLED;
        }
        else if (!update.equals("Unknown") && !update.equals(version))
        {
            notice = " " + Message.OUTDATED;
        }

        sender.sendMessage(color("&8&m     &6 Coins &e" + version + " &8&m     &4" + notice));

        if (sender.hasPermission("coins.drop"))
        {
            sender.sendMessage(Message.DROP_USAGE.toString());
        }

        if (sender.hasPermission("coins.remove"))
        {
            sender.sendMessage(Message.REMOVE_USAGE.toString());
        }

        if (sender.hasPermission("coins.admin"))
        {
            sender.sendMessage(Message.SETTINGS_USAGE.toString());
            sender.sendMessage(Message.RELOAD_USAGE.toString());
            sender.sendMessage(Message.VERSION_CHECK.toString());
        }

        if (sender.hasPermission("coins.toggle"))
        {
            sender.sendMessage(Message.TOGGLE_USAGE.toString());
        }

        if (Config.ENABLE_WITHDRAW && sender.hasPermission("coins.withdraw"))
        {
            sender.sendMessage(Message.WITHDRAW_USAGE.toString());
        }
    }

    private void noPerm (CommandSender sender)
    {
        sender.sendMessage(Message.NO_PERMISSION.toString());
    }

    public void dropCoins (final Location location, final int radius, final int amount)
    {
        final Coins coins = this.coins;
        final Location dropLocation = location.clone().add(0.0, 0.5, 0.0);
        final ItemStack coin = coins.getCreateCoin().dropped();

        AtomicInteger ticks = new AtomicInteger();
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                Item item = dropLocation.getWorld().dropItem(
                        dropLocation,
                        coins.meta(coin).data(CoinUtil.COINS_RANDOM, RANDOM.nextDouble()).build()
                );

                item.setPickupDelay(30);
                item.setVelocity(new Vector(
                        (RANDOM.nextDouble() - 0.5) * radius / 10,
                        RANDOM.nextDouble() * radius / 5,
                        (RANDOM.nextDouble() - 0.5) * radius / 10
                ));

                if (ticks.addAndGet(1) >= amount)
                {
                    this.cancel();
                }
            }
        }.runTaskTimer(this.coins, 0, 1);
    }
}
