package me.justeli.coins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.lib.PaperLib;
import me.justeli.coins.command.CoinsDisabled;
import me.justeli.coins.handler.HopperHandler;
import me.justeli.coins.handler.InventoryHandler;
import me.justeli.coins.handler.InteractionHandler;
import me.justeli.coins.handler.ModificationHandler;
import me.justeli.coins.handler.UnfairMobHandler;
import me.justeli.coins.handler.listener.BukkitEventListener;
import me.justeli.coins.handler.PickupHandler;
import me.justeli.coins.handler.DropHandler;
import me.justeli.coins.handler.listener.PaperEventListener;
import me.justeli.coins.command.Commands;
import me.justeli.coins.command.TabComplete;
import me.justeli.coins.hook.MythicMobsHook;
import me.justeli.coins.hook.bStatsMetrics;
import me.justeli.coins.config.Config;
import me.justeli.coins.config.Settings;
import me.justeli.coins.item.BaseCoin;
import me.justeli.coins.util.Reloadable;
import me.justeli.coins.util.Util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/** by Eli on 12/13/2016. **/
public class Coins
        extends JavaPlugin
        implements Reloadable
{
    // TODO
    // - add option to not let balance go negative (with dropOnDeath: true)

    private static Coins PLUGIN;
    private static Economy ECONOMY;
    private static String LATEST = "Unknown";

    private final static List<String> DISABLED_REASONS = new ArrayList<>();

    public static List<String> getDisabledReasons ()
    {
        return DISABLED_REASONS;
    }

    public static Coins plugin ()
    {
        return PLUGIN;
    }

    public static Economy economy ()
    {
        return ECONOMY;
    }

    public static String latest ()
    {
        return LATEST;
    }

    private static final String UNSUPPORTED_VERSION = "Coins only supports Minecraft version 1.14 and higher. For 1.8.8 to 1.13.2 support, you can " +
            "use Coins version 1.10.8.";
    private static final String USING_BUKKIT = "You seem to be using Bukkit, but the plugin Coins requires at least Spigot! " +
            "This prevents the plugin from showing the amount of money players pick up. Please use Spigot or Paper. Moving from Bukkit to " +
            "Spigot will NOT cause any problems with other plugins, since Spigot only adds more features to Bukkit.";
    private static final String LACKING_ECONOMY = "There is no proper economy installed. Please install %s.";

    @Override
    public void onEnable ()
    {
        Locale.setDefault(Locale.US);
        PLUGIN = this;

        if (PaperLib.getMinecraftVersion() < 14)
        {
            line(Level.SEVERE);
            console(Level.SEVERE, UNSUPPORTED_VERSION);
            disablePlugin(UNSUPPORTED_VERSION);
        }

        if (!PaperLib.isSpigot() && !PaperLib.isPaper())
        {
            line(Level.SEVERE);
            console(Level.SEVERE, USING_BUKKIT);
            disablePlugin(USING_BUKKIT);
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            noEconomySupport("Vault");
        }

        try
        {
            RegisteredServiceProvider<Economy> service = getServer().getServicesManager().getRegistration(Economy.class);
            ECONOMY = service.getProvider();
        }
        catch (NullPointerException | NoClassDefFoundError throwable)
        {
            noEconomySupport("an economy supportive plugin");
        }

        if (!PaperLib.isPaper())
        {
            PaperLib.suggestPaper(this);
            console(Level.WARNING, "Players with a full inventory will be able to pick up coins when Paper is installed.");
        }

        if (getServer().getPluginManager().getPlugin("MythicMobs") != null)
        {
            enableMythicMobs();
        }

        if (DISABLED_REASONS.size() == 0)
        {
            onReload();

            registerEvents();
            registerCommands();

            runAsync(this::versionChecker);
            runAsync(bStatsMetrics::register);
        }
        else
        {
            CoinsDisabled coinsDisabled = new CoinsDisabled();

            this.getCommand("coins").setExecutor(coinsDisabled);
            this.getCommand("withdraw").setExecutor(coinsDisabled);

            line(Level.SEVERE);
            console(Level.SEVERE, "Plugin 'Coins' is now disabled, until the issues are fixed.");
            line(Level.SEVERE);
        }
    }

    @Override
    public void onReload ()
    {
        if (DISABLED_REASONS.size() != 0)
        {
            line(Level.SEVERE);
            console(Level.SEVERE, "Plugin 'Coins' is disabled, until issues are fixed and the server is rebooted (see start-up log of Coins).");
            line(Level.SEVERE);
            return;
        }

        Util.resetMultiplier();
        Settings.reload();

        this.baseCoin = BaseCoin.initialize();

        if (Config.getWarningCount() != 0)
        {
            console(Level.SEVERE, "Loaded the config of Coins with " + Config.getWarningCount() + " warnings. Check above here for details.");
        }
    }

    private void noEconomySupport (String kind)
    {
        line(Level.SEVERE);

        String reason = String.format(LACKING_ECONOMY, kind);

        console(Level.SEVERE, reason);
        disablePlugin(reason);
    }

    private void line (Level type)
    {
        console(type, "------------------------------------------------------------------");
    }

    private void disablePlugin (String reason)
    {
        DISABLED_REASONS.add(reason);
    }

    private void versionChecker ()
    {
        try
        {
            URL url = new URL("https://api.github.com/repos/JustEli/Coins/releases/latest");
            URLConnection request = url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            Coins.LATEST = rootobj.get("tag_name").getAsString();
        }
        catch (IOException ignored) {}

        if (!Coins.LATEST.equals("Unknown") && !getDescription().getVersion().equals(Coins.LATEST))
        {
            line(Level.WARNING);
            getLogger().warning("   You're running an outdated version of Coins 1.x.");
            getLogger().warning("   The version installed is " + getDescription().getVersion() + ", while " + Coins.LATEST + " is out!");
            getLogger().warning("   https://www.spigotmc.org/resources/coins.33382/");
            line(Level.WARNING);
        }
    }

    private void registerEvents ()
    {
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(PaperLib.isPaper()? new PaperEventListener() : new BukkitEventListener(), this);

        manager.registerEvents(new HopperHandler(), this);
        manager.registerEvents(new UnfairMobHandler(), this);
        manager.registerEvents(new PickupHandler(), this);
        manager.registerEvents(new DropHandler(), this);
        manager.registerEvents(new InteractionHandler(), this);
        manager.registerEvents(new InventoryHandler(), this);
        manager.registerEvents(new ModificationHandler(), this);

        if (hasMythicMobs())
        {
            manager.registerEvents(new MythicMobsHook(), this);
        }
    }

    private void registerCommands ()
    {
        this.getCommand("coins").setExecutor(new Commands());
        this.getCommand("coins").setTabCompleter(new TabComplete());

        if (Config.ENABLE_WITHDRAW)
        {
            this.getCommand("withdraw").setExecutor(new Commands());
            this.getCommand("withdraw").setTabCompleter(new TabComplete());
        }
    }

    private static void runAsync (final Runnable runnable)
    {
        PLUGIN.getServer().getScheduler().runTaskAsynchronously(PLUGIN, runnable);
    }

    public static void runLater (final int ticks, final Runnable runnable)
    {
        PLUGIN.getServer().getScheduler().runTaskLater(PLUGIN, runnable, ticks);
    }

    public static void console (Level type, String message)
    {
        PLUGIN.getLogger().log(type, message);
    }

    private static boolean DISABLED = false;

    public static boolean isDisabled ()
    {
        return DISABLED;
    }

    public static boolean toggleDisabled ()
    {
        DISABLED = !DISABLED;
        return !DISABLED;
    }

    private static boolean MYTHIC_MOBS = false;

    public static boolean hasMythicMobs ()
    {
        return MYTHIC_MOBS;
    }

    public static void enableMythicMobs ()
    {
        MYTHIC_MOBS = true;
    }

    private BaseCoin baseCoin;

    public BaseCoin getBaseCoin ()
    {
        return baseCoin;
    }
}
