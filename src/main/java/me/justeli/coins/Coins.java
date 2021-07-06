package me.justeli.coins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.lib.PaperLib;
import me.justeli.coins.cancel.CancelHopper;
import me.justeli.coins.cancel.CancelInventories;
import me.justeli.coins.cancel.CoinPlace;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.events.BukkitEvents;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.events.DropCoin;
import me.justeli.coins.events.PaperEvents;
import me.justeli.coins.main.Cmds;
import me.justeli.coins.main.Metrics;
import me.justeli.coins.main.TabComplete;
import me.justeli.coins.mythicmobs.MythicMobsHook;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.WordUtils;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Created by Eli on 12/13/2016.
 */

public class Coins
        extends JavaPlugin
{
    // TODO
    // - add option to not let balance go negative (with dropOnDeath: true)

    private static Coins PLUGIN;
    private static Economy ECONOMY;
    private static String LATEST;

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

    @Override
    public void onEnable ()
    {
        Locale.setDefault(Locale.US);
        PLUGIN = this;

        if (PaperLib.getMinecraftVersion() < 8 || (PaperLib.getMinecraftVersion() == 8 && PaperLib.getMinecraftPatchVersion() < 8))
        {
            line(Level.SEVERE);
            getLogger().log(Level.SEVERE, "COINS ONLY SUPPORTS MINECRAFT VERSION 1.8.8 AND UP.");

            disablePlugin();
            return;
        }

        if (!PaperLib.isSpigot() && !PaperLib.isPaper())
        {
            line(Level.SEVERE);
            getLogger().log(Level.SEVERE, "You seem to be using Bukkit, but the plugin Coins requires at least Spigot! " +
                    "This prevents the plugin from showing the amount of money players pick up. Please use Spigot. Moving from Bukkit to " +
                    "Spigot will NOT cause any problems with other plugins, since Spigot only adds more features to Bukkit.");

            disablePlugin();
            return;
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            line(Level.SEVERE);
            Settings.errorMessage(Settings.Msg.NO_ECONOMY_SUPPORT, new String[]{""});

            disablePlugin();
            return;
        }

        try
        {
            RegisteredServiceProvider<Economy> service = getServer().getServicesManager().getRegistration(Economy.class);
            ECONOMY = service.getProvider();
        }
        catch (NullPointerException | NoClassDefFoundError e)
        {
            line(Level.SEVERE);
            Settings.errorMessage(Settings.Msg.NO_ECONOMY_SUPPORT, new String[]{""});

            disablePlugin();
            return;
        }

        if (PaperLib.getMinecraftVersion() >= 13 && !PaperLib.isPaper())
        {
            PaperLib.suggestPaper(this);
            getLogger().warning("Players with a full inventory will be able to pick up coins when Paper is installed.");
        }

        if (getServer().getPluginManager().getPlugin("MythicMobs") != null)
        {
            enableMythicMobs();
        }
        else {
            getLogger().info("MythicMobsHook not Enabled");
        }

        Settings.load();
        registerEvents();
        registerCommands();


        async(this::versionChecker);
        async(this::metrics);
    }

    private void line (Level type)
    {
        getLogger().log(type, "------------------------------------------------------------------");
    }

    private void disablePlugin ()
    {
        line(Level.SEVERE);
        getLogger().log(Level.SEVERE, "PLUGIN 'COINS' WILL BE DISABLED NOW!");
        line(Level.SEVERE);

        getServer().getPluginManager().disablePlugin(this);
    }

    private void versionChecker ()
    {
        String version;
        try
        {
            URL url = new URL("https://api.github.com/repos/JustEli/Coins/releases/latest");
            URLConnection request = url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            version = rootobj.get("tag_name").getAsString();
        }
        catch (IOException ex)
        {
            version = getDescription().getVersion();
        }

        Coins.LATEST = version;

        if (!getDescription().getVersion().equals(version))
        {
            line(Level.WARNING);
            getLogger().warning("   You're running an outdated version of Coins 1.x.");
            getLogger().warning("   The version installed is " + getDescription().getVersion() + ", while " + version + " is out!");
            getLogger().warning("   https://www.spigotmc.org/resources/coins.33382/");
            line(Level.WARNING);
        }
    }

    private void metrics ()
    {
        Metrics metrics = new Metrics(this);
        String texture = Settings.hS.get(Config.STRING.skullTexture);

        metrics.add("language", WordUtils.capitalize(Settings.getLanguage()));
        metrics.add("currencySymbol", Settings.hS.get(Config.STRING.currencySymbol));
        metrics.add("dropChance", Settings.hD.get(Config.DOUBLE.dropChance) * 100 + "%");
        metrics.add("dropEachCoin", String.valueOf(Settings.hB.get(Config.BOOLEAN.dropEachCoin)));
        metrics.add("pickupSound", Settings.hS.get(Config.STRING.soundName));
        metrics.add("enableWithdraw", String.valueOf(Settings.hB.get(Config.BOOLEAN.enableWithdraw)));
        metrics.add("loseOnDeath", String.valueOf(Settings.hB.get(Config.BOOLEAN.loseOnDeath)));
        metrics.add("passiveDrop", String.valueOf(Settings.hB.get(Config.BOOLEAN.passiveDrop)));

        metrics.add("nameOfCoin", Settings.hS.get(Config.STRING.nameOfCoin));
        metrics.add("coinItem", Settings.hS.get(Config.STRING.coinItem));
        metrics.add("pickupMessage", Settings.hS.get(Config.STRING.pickupMessage));
        metrics.add("moneyDecimals", String.valueOf(Settings.hD.get(Config.DOUBLE.moneyDecimals).intValue()));
        metrics.add("stackCoins", String.valueOf(Settings.hB.get(Config.BOOLEAN.stackCoins)));
        metrics.add("playerDrop", String.valueOf(Settings.hB.get(Config.BOOLEAN.playerDrop)));
        metrics.add("spawnerDrop", String.valueOf(Settings.hB.get(Config.BOOLEAN.spawnerDrop)));
        metrics.add("preventSplits", String.valueOf(Settings.hB.get(Config.BOOLEAN.preventSplits)));

        metrics.add("moneyAmount", (String.valueOf((Settings.hD.get(Config.DOUBLE.moneyAmount_from) +
                Settings.hD.get(Config.DOUBLE.moneyAmount_to)) / 2)));
        metrics.add("usingSkullTexture", String.valueOf(texture != null && !texture.isEmpty()));
        metrics.add("disableHoppers", String.valueOf(Settings.hB.get(Config.BOOLEAN.disableHoppers)));
        metrics.add("dropWithAnyDeath", String.valueOf(Settings.hB.get(Config.BOOLEAN.dropWithAnyDeath)));
        metrics.add("usingPaper", String.valueOf(PaperLib.isPaper()));
    }

    private void registerEvents ()
    {
        PluginManager manager = getServer().getPluginManager();

        boolean validPaper = PaperLib.isPaper() && PaperLib.getMinecraftVersion() > 12;

        manager.registerEvents(validPaper? new PaperEvents() : new BukkitEvents(), this);

        manager.registerEvents(new CancelHopper(), this);
        manager.registerEvents(new PreventSpawner(), this);
        manager.registerEvents(new CoinsPickup(), this);
        manager.registerEvents(new DropCoin(), this);
        manager.registerEvents(new CoinPlace(), this);
        manager.registerEvents(new CancelInventories(), this);

        if(hasMythMobs())
            new MythicMobsHook().Enable(this);
    }

    private void registerCommands ()
    {
        this.getCommand("coins").setExecutor(new Cmds());
        this.getCommand("coins").setTabCompleter(new TabComplete());

        if (Settings.hB.get(Config.BOOLEAN.enableWithdraw))
        {
            this.getCommand("withdraw").setExecutor(new Cmds());
            this.getCommand("withdraw").setTabCompleter(new TabComplete());
        }
    }

    private static void async (Runnable runnable)
    {
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    public static void later (final int ticks, Runnable runnable)
    {
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTaskLater(PLUGIN, ticks);
    }

    public static void console (Level type, String message)
    {
        PLUGIN.getLogger().log(type, message);
    }

    private static final AtomicBoolean DISABLED = new AtomicBoolean(false);

    public static boolean isDisabled ()
    {
        return DISABLED.get();
    }

    public static boolean toggleDisabled () { return DISABLED.getAndSet(!DISABLED.get()); }
    private static final AtomicBoolean MYTHICMOBS = new AtomicBoolean(false);

    public static boolean hasMythMobs () { return MYTHICMOBS.get(); }

    public static void enableMythicMobs () { MYTHICMOBS.set(true); }
}
