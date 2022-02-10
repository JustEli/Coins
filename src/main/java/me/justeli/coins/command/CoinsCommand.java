package me.justeli.coins.command;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import me.justeli.coins.config.Message;
import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.util.Permission;
import me.justeli.coins.util.Util;
import me.justeli.coins.util.VersionChecker;
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
import java.util.Date;
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
        if (args.length == 0)
        {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT))
        {
            case "reload":
                if (perm(sender, Permission.COMMAND_RELOAD))
                {
                    long ms = System.currentTimeMillis();

                    this.coins.reload();
                    int warnings = this.coins.settings().getWarningCount();

                    sender.sendMessage(Message.RELOAD_SUCCESS.replace(Long.toString(System.currentTimeMillis() - ms)));
                    if (warnings != 0)
                    {
                        sender.sendMessage(Message.MINOR_ISSUES.toString());
                    }
                    else
                    {
                        sender.sendMessage(Message.CHECK_SETTINGS.toString());
                    }
                }
                break;
            case "settings":
                if (perm(sender, Permission.COMMAND_SETTINGS))
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
                break;
            case "drop":
                if (perm(sender, Permission.COMMAND_DROP))
                {
                    dropCoinsCommand(sender, args);
                }
                break;
            case "remove":
                if (perm(sender, Permission.COMMAND_REMOVE))
                {
                    removeCoins(sender, args);
                }
                break;
            case "lang":
            case "language":
                if (perm(sender, Permission.COMMAND_LANGUAGE))
                {
                    for (Message message : Message.values())
                    {
                        sender.sendMessage(message.toString());
                    }
                }
                break;
            case "version":
            case "update":
                if (perm(sender, Permission.COMMAND_VERSION))
                {
                    sender.sendMessage(String.format(COINS_TITLE, "Version"));

                    Optional<VersionChecker.Version> latestVersion = this.coins.latestVersion();
                    String currentVersion = this.coins.getDescription().getVersion();

                    sender.sendMessage(Message.CURRENTLY_INSTALLED.replace(currentVersion));

                    if (!latestVersion.isPresent())
                    {
                        sender.sendMessage(Message.LATEST_RETRIEVE_FAIL.toString());
                    }
                    else if (latestVersion.get().tag().equals(currentVersion))
                    {
                        sender.sendMessage(Message.UP_TO_DATE.replace(currentVersion));
                    }
                    else
                    {
                        sender.sendMessage(Message.LATEST_RELEASE.replace(
                                latestVersion.get().tag(),
                                Util.DATE_FORMAT.format(new Date(latestVersion.get().time())),
                                latestVersion.get().name(),
                                this.coins.getDescription().getWebsite()
                        ));
                    }
                }
                break;
            case "toggle":
                if (perm(sender, Permission.COMMAND_TOGGLE))
                {
                    Message abled = this.coins.toggleDisabled()? Message.ENABLED : Message.DISABLED;
                    sender.sendMessage(Message.GLOBALLY_DISABLED_INFORM.replace(abled.toString()));
                    if (this.coins.isDisabled())
                    {
                        sender.sendMessage(Message.DISABLED_DESCRIPTION.toString());
                    }
                }
                break;
            default:
            {
                sendHelp(sender);
                break;
            }
        }

        return true;
    }

    private boolean perm (CommandSender sender, String permission)
    {
        if (sender.hasPermission(permission))
            return true;

        sender.sendMessage(Message.NO_PERMISSION.toString());
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        List<String> list = new ArrayList<>();
        if (args.length == 1)
        {
            if (sender.hasPermission(Permission.COMMAND_DROP))
            {
                list.add("drop");
            }
            if (sender.hasPermission(Permission.COMMAND_RELOAD))
            {
                list.add("reload");
            }
            if (sender.hasPermission(Permission.COMMAND_SETTINGS))
            {
                list.add("settings");
            }
            if (sender.hasPermission(Permission.COMMAND_VERSION))
            {
                list.add("version");
            }
            if (sender.hasPermission(Permission.COMMAND_REMOVE))
            {
                list.add("remove");
            }
            if (sender.hasPermission(Permission.COMMAND_TOGGLE))
            {
                list.add("toggle");
            }
        }
        else if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("remove") && sender.hasPermission(Permission.COMMAND_REMOVE))
            {
                list.add("all");
                list.add("[radius]");
            }
            if (args[0].equalsIgnoreCase("drop") && sender.hasPermission(Permission.COMMAND_DROP))
            {
                for (Player onlinePlayer : this.coins.getServer().getOnlinePlayers())
                {
                    list.add(onlinePlayer.getName());
                }
                list.add("<x,y,z>");
                list.add("<x,y,z,world>");
            }
            if (args[0].equalsIgnoreCase("settings") && sender.hasPermission(Permission.COMMAND_SETTINGS))
            {
                list.add("1"); list.add("2"); list.add("3"); list.add("4"); list.add("5"); list.add("6");
            }
        }
        else if (args.length == 3)
        {
            if (args[0].equalsIgnoreCase("remove") && sender.hasPermission(Permission.COMMAND_REMOVE))
            {
                list.add("<amount>");
            }
        }
        else if (args.length == 4)
        {
            if (args[0].equalsIgnoreCase("remove") && sender.hasPermission(Permission.COMMAND_REMOVE))
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
        String currentVersion = this.coins.getDescription().getVersion();
        Optional<VersionChecker.Version> latestVersion = this.coins.latestVersion();

        int lines = 0;

        String notice = "";
        if (this.coins.isDisabled())
        {
            notice = " " + Message.GLOBALLY_DISABLED;
        }
        else if (latestVersion.isPresent() && !latestVersion.get().tag().equals(currentVersion) && sender.hasPermission(Permission.COMMAND_VERSION))
        {
            notice = " " + Message.OUTDATED.replace("/coins update");
        }

        sender.sendMessage(String.format(COINS_TITLE, currentVersion) + ChatColor.DARK_RED + notice);

        if (sender.hasPermission(Permission.COMMAND_DROP))
        {
            sender.sendMessage(Message.DROP_USAGE.toString());
            lines++;
        }
        if (sender.hasPermission(Permission.COMMAND_REMOVE))
        {
            sender.sendMessage(Message.REMOVE_USAGE.toString());
            lines++;
        }
        if (sender.hasPermission(Permission.COMMAND_SETTINGS))
        {
            sender.sendMessage(Message.SETTINGS_USAGE.toString());
            lines++;
        }
        if (sender.hasPermission(Permission.COMMAND_RELOAD))
        {
            sender.sendMessage(Message.RELOAD_USAGE.toString());
            lines++;
        }
        if (sender.hasPermission(Permission.COMMAND_VERSION))
        {
            sender.sendMessage(Message.VERSION_CHECK.toString());
            lines++;
        }
        if (sender.hasPermission(Permission.COMMAND_TOGGLE))
        {
            sender.sendMessage(Message.TOGGLE_USAGE.toString());
            lines++;
        }
        if (Config.ENABLE_WITHDRAW && sender.hasPermission(Permission.WITHDRAW))
        {
            sender.sendMessage(Message.WITHDRAW_USAGE.toString());
            lines++;
        }

        if (lines == 0)
        {
            sender.sendMessage(ChatColor.GOLD + this.coins.getDescription().getDescription());
            sender.sendMessage(ChatColor.YELLOW + "More info: " + ChatColor.BLUE + this.coins.getDescription().getWebsite());
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
}
