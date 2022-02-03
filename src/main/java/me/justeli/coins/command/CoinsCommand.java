package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import me.justeli.coins.config.Message;
import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/** Created by Eli on 26 dec 2018. */
public final class CoinsCommand
        implements CommandExecutor, TabCompleter
{
    private final Coins coins;
    private final PluginCommand command;

    public CoinsCommand (Coins coins)
    {
        this.coins = coins;
        this.command = coins.getCommand("coins");
    }

    public PluginCommand command ()
    {
        return command;
    }

    private final static SplittableRandom RANDOM = new SplittableRandom();

    @Override
    public boolean onCommand (@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if (args.length >= 1)
        {
            switch (args[0].toLowerCase(Locale.ROOT))
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
                        int page = args.length > 1? Util.parseInt(args[1]).orElse(1) : 1;
                        TreeSet<String> keys = this.coins.settings().getKeys();
                        int totalPages = keys.size() / 8 + Math.min(keys.size() % 8, 1);

                        sender.sendMessage(String.format(COINS_TITLE, "Settings") + Util.color(" &7" + page + "&8/&7" + totalPages));
                        for (String setting : Util.page(new ArrayList<>(keys), 8, page))
                        {
                            sender.sendMessage(Util.color(setting));
                        }
                    }
                    else
                    {
                        noPerm(sender);
                    }
                    break;
                case "drop":
                    if (sender.hasPermission("coins.drop"))
                        dropCoinsCommand(sender, args);
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
                            sender.sendMessage(Util.color("&9https://www.spigotmc.org/resources/coins.33382/"));
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
                {
                    sendHelp(sender);
                    break;
                }
            }
        }
        else
        {
            sendHelp(sender);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        List<String> list = new ArrayList<>();
        if (args.length == 1)
        {
            if (sender.hasPermission("coins.drop"))
            {
                list.add("drop");
            }
            if (sender.hasPermission("coins.admin"))
            {
                list.add("reload");
                list.add("settings");
                list.add("version");
            }
            if (sender.hasPermission("coins.remove"))
            {
                list.add("remove");
            }
            if (sender.hasPermission("coins.toggle"))
            {
                list.add("toggle");
            }
        }
        else if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("remove") && sender.hasPermission("coins.remove"))
            {
                list.add("all");
                list.add("[radius]");
            }
            if (args[0].equalsIgnoreCase("drop"))
            {
                for (Player onlinePlayer : this.coins.getServer().getOnlinePlayers())
                {
                    list.add(onlinePlayer.getName());
                }
                list.add("<x,y,z>");
                list.add("<x,y,z,world>");
            }
            if (args[0].equalsIgnoreCase("settings") && sender.hasPermission("coins.admin"))
            {
                list.add("1"); list.add("2"); list.add("3"); list.add("4"); list.add("5"); list.add("6");
            }
        }
        else if (args.length == 3)
        {
            if (args[0].equalsIgnoreCase("remove") && sender.hasPermission("coins.remove"))
            {
                list.add("<amount>");
            }
        }
        else if (args.length == 4)
        {
            if (args[0].equalsIgnoreCase("remove") && sender.hasPermission("coins.remove"))
            {
                list.add("[radius]");
            }
        }

        return list;
    }

    private void dropCoinsCommand (CommandSender sender, String[] args)
    {
        if (args.length >= 3)
        {
            Player onlinePlayer = Util.getOnlinePlayer(args[1]);

            Optional<Integer> amount = Util.parseInt(args[2]);
            if (!amount.isPresent())
            {
                sender.sendMessage(Message.INVALID_NUMBER.toString());
                return;
            }

            int radius = amount.get() / 20;
            if (radius < 2)
            {
                radius = 2;
            }

            if (args.length >= 4)
            {
                Optional<Integer> r = Util.parseInt(args[3]);
                if (!r.isPresent())
                {
                    sender.sendMessage(Message.INVALID_NUMBER.toString());
                    return;
                }

                radius = r.get();
            }

            Location location;
            String name;
            if (onlinePlayer == null)
            {
                if (!args[1].contains(","))
                {
                    sender.sendMessage(Message.PLAYER_NOT_FOUND.toString());
                    return;
                }
                else
                {
                    String[] coords = args[1].split(",");

                    Optional<Double> x = Util.parseDouble(coords[0]);
                    Optional<Double> y = Util.parseDouble(coords[1]);
                    Optional<Double> z = Util.parseDouble(coords[2]);

                    if (x.isPresent() && y.isPresent() && z.isPresent())
                    {
                        World world = null;
                        if (coords.length == 4)
                        {
                            world = this.coins.getServer().getWorld(coords[3]);
                        }
                        if (world == null && sender instanceof Player)
                        {
                            world = ((Player) sender).getWorld();
                        }
                        if (world == null)
                        {
                            world = this.coins.getServer().getWorlds().get(0);
                        }

                        location = new Location(world, x.get(), y.get(), z.get());
                        name = Util.doubleToString(x.get()) + ", " + Util.doubleToString(y.get()) + ", " + Util.doubleToString(z.get());
                    }
                    else
                    {
                        sender.sendMessage(Message.COORDS_NOT_FOUND.toString());
                        return;
                    }
                }

            }
            else
            {
                location = onlinePlayer.getLocation();
                name = onlinePlayer.getName();
            }

            if (Util.isDisabledHere(location.getWorld()))
            {
                sender.sendMessage(Message.COINS_DISABLED.toString());
                return;
            }

            if (radius < 1 || radius > 80)
            {
                sender.sendMessage(Message.INVALID_RADIUS.toString());
                return;
            }

            if (amount.get() < 1 || amount.get() > 1000)
            {
                sender.sendMessage(Message.INVALID_AMOUNT.toString());
                return;
            }

            dropCoins(location, radius, amount.get());
            sender.sendMessage(
                    Message.SPAWNED_COINS.replace(
                            Long.toString(amount.get()),
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
                Optional<Integer> r2 = Util.parseInt(args[1]);
                if (!r2.isPresent())
                {
                    sender.sendMessage(Message.INVALID_RADIUS.toString());
                    return;
                }
                r = r2.get();

                if (r < 1 || r > 80)
                {
                    sender.sendMessage(Message.INVALID_RADIUS.toString());
                    return;
                }
            }

        }

        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            mobs = player.getWorld().getEntities();
            if (r != 0)
            {
                mobs = new ArrayList<>(player.getWorld().getNearbyEntities(player.getLocation(), r, r, r));
            }
        }

        long amount = 0;
        for (Entity entity : mobs)
        {
            if (entity instanceof Item)
            {
                Item item = (Item) entity;
                if (this.coins.getCoinUtil().isCoin(item.getItemStack()))
                {
                    amount++;
                    double random = (RANDOM.nextDouble() * 3);
                    long rand = (long) random * 5;
                    item.setVelocity(new Vector(0, random, 0));
                    new BukkitRunnable()
                    {
                        int a = 0;

                        public void run ()
                        {
                            a += 1;
                            if (a >= 1)
                            {
                                item.remove();
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(this.coins, rand, rand);
                }
            }
        }
        sender.sendMessage(Message.REMOVED_COINS.replace(Long.toString(amount)));
    }

    private static final String COINS_TITLE = Util.color("&8&m     &6 Coins &e%s &8&m     &r");

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

        sender.sendMessage(String.format(COINS_TITLE, version) + ChatColor.DARK_RED + notice);

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

    private void noPerm (CommandSender sender)
    {
        sender.sendMessage(Message.NO_PERMISSION.toString());
    }
}
