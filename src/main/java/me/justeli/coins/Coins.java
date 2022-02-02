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
import me.justeli.coins.hook.Metrics;
import me.justeli.coins.config.Config;
import me.justeli.coins.config.Settings;
import me.justeli.coins.item.BaseCoin;
import me.justeli.coins.hook.Economies;
import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.item.CreateCoin;
import me.justeli.coins.item.MetaBuilder;
import me.justeli.coins.util.Reloadable;
import me.justeli.coins.util.Util;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
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

    private static final String UNSUPPORTED_VERSION = "Coins only supports Minecraft version 1.14 and higher. For 1.8.8 to 1.13.2 support, you can " +
            "use Coins version 1.10.8.";
    private static final String USING_BUKKIT = "You seem to be using Bukkit, but the plugin Coins requires at least Spigot! " +
            "This prevents the plugin from showing the amount of money players pick up. Please use Spigot or Paper. Moving from Bukkit to " +
            "Spigot will NOT cause any problems with other plugins, since Spigot only adds more features to Bukkit.";
    private static final String LACKING_ECONOMY = "There is no proper economy installed. Please install %s.";

    @Override
    public void onEnable ()
    {
        long current = System.currentTimeMillis();
        Locale.setDefault(Locale.US);

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

        this.economy = Economies.of(this);
        for (String missingPlugin : this.economy.getMissingPluginNames())
        {
            noEconomySupport(missingPlugin);
        }

        if (!PaperLib.isPaper())
        {
            PaperLib.suggestPaper(this);
            console(Level.WARNING, "Players with a full inventory will be able to pick up coins when Paper is installed.");
        }

        if (getServer().getPluginManager().isPluginEnabled("MythicMobs"))
        {
            enableMythicMobs();
        }

        if (this.disabledReasons.size() == 0)
        {
            this.settings = new Settings(this);
            onReload();

            registerEvents();
            registerCommands();

            async(() ->
            {
                versionChecker();
                new Metrics(this).register();
            });
        }
        else
        {
            CoinsDisabled coinsDisabled = new CoinsDisabled(this);

            this.getCommand("coins").setExecutor(coinsDisabled);
            this.getCommand("withdraw").setExecutor(coinsDisabled);

            line(Level.SEVERE);
            console(Level.SEVERE, "Plugin 'Coins' is now disabled, until the issues are fixed.");
            line(Level.SEVERE);
        }
        console(Level.INFO, "Initialized in " + (System.currentTimeMillis() - current) + "ms.");
    }

    @Override
    public void onReload ()
    {
        if (this.disabledReasons.size() != 0)
        {
            line(Level.SEVERE);
            console(Level.SEVERE, "Plugin 'Coins' is disabled, until issues are fixed and the server is rebooted (see start-up log of Coins).");
            line(Level.SEVERE);
            return;
        }

        Util.resetMultiplier();

        this.settings.reloadLanguage();
        this.settings.resetWarningCount();
        this.settings.parseConfig();

        this.baseCoin = BaseCoin.initialize(this);
        this.createCoin = new CreateCoin(this);
        this.coinUtil = new CoinUtil(this);

        if (this.settings.getWarningCount() != 0)
        {
            console(Level.SEVERE, "Loaded the config of Coins with " + this.settings.getWarningCount() + " warnings. Check above here for details.");
        }

        if (Config.DETECT_LEGACY_COINS)
        {
            console(Level.WARNING, "Coins has been configured to detected legacy withdrawn coins, which are coins withdrawn before Coins version " +
                    "1.11. It is recommended to tell your players to deposit all their legacy withdrawn coins, as support for legacy withdrawn " +
                    "coins by this plugin will be dropped early 2023. If you do not wish to detect legacy withdrawn coins, you can set " +
                    "`detectLegacyCoins` to false in the config, which is safer, and prevents possible exploits (if plugins are installed that " +
                    "allow items to be renamed in color).");
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
        disabledReasons.add(reason);
    }

    private void versionChecker ()
    {
        if (!Config.CHECK_FOR_UPDATES)
            return;

        try
        {
            URL url = new URL("https://api.github.com/repos/JustEli/Coins/releases/latest");
            URLConnection request = url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            try (
                    InputStream inputStream = (InputStream) request.getContent();
                    InputStreamReader reader = new InputStreamReader(inputStream);
            )
            {
                JsonElement root = jp.parse(reader);
                JsonObject rootobj = root.getAsJsonObject();
                this.latest = rootobj.get("tag_name").getAsString();
            }
        }
        catch (IOException ignored) {}

        if (!this.latest.equals("Unknown") && !getDescription().getVersion().equals(this.latest))
        {
            line(Level.WARNING);
            console(Level.WARNING, "   You're running an outdated version of Coins 1.x.");
            console(Level.WARNING, "   The version installed is " + getDescription().getVersion() + ", while " + this.latest + " is out!");
            console(Level.WARNING, "   https://www.spigotmc.org/resources/coins.33382/");
            line(Level.WARNING);
        }
    }

    private void registerEvents ()
    {
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(PaperLib.isPaper()? new PaperEventListener(this) : new BukkitEventListener(this), this);

        this.unfairMobHandler = new UnfairMobHandler(this);
        this.pickupHandler = new PickupHandler(this);

        manager.registerEvents(new HopperHandler(this), this);
        manager.registerEvents(this.unfairMobHandler, this);
        manager.registerEvents(this.pickupHandler, this);
        manager.registerEvents(new DropHandler(this), this);
        manager.registerEvents(new InteractionHandler(this), this);
        manager.registerEvents(new InventoryHandler(this), this);
        manager.registerEvents(new ModificationHandler(this), this);

        if (hasMythicMobs())
        {
            manager.registerEvents(new MythicMobsHook(this), this);
        }
    }

    private void registerCommands ()
    {
        Commands commands = new Commands(this);
        TabComplete tabComplete = new TabComplete();

        this.getCommand("coins").setExecutor(commands);
        this.getCommand("coins").setTabCompleter(tabComplete);

        if (Config.ENABLE_WITHDRAW)
        {
            this.getCommand("withdraw").setExecutor(commands);
            this.getCommand("withdraw").setTabCompleter(tabComplete);
        }
    }

    public void async (final Runnable runnable)
    {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    public void sync (final int ticks, final Runnable runnable)
    {
        getServer().getScheduler().runTaskLater(this, runnable, ticks);
    }

    public void console (Level type, String message)
    {
        getLogger().log(type, message);
    }

    private Economies economy;
    public Economies economy ()
    {
        return this.economy;
    }

    private String latest = "Unknown";
    public String latest ()
    {
        return this.latest;
    }

    private final List<String> disabledReasons = new ArrayList<>();
    public List<String> disabledReasons ()
    {
        return this.disabledReasons;
    }

    private boolean pluginDisabled = false;

    public boolean isDisabled ()
    {
        return this.pluginDisabled;
    }

    public boolean toggleDisabled ()
    {
        this.pluginDisabled = !this.pluginDisabled;
        return !this.pluginDisabled;
    }

    private boolean mythicMobsHook = false;

    public boolean hasMythicMobs ()
    {
        return this.mythicMobsHook;
    }

    public void enableMythicMobs ()
    {
        this.mythicMobsHook = true;
    }



    private BaseCoin baseCoin;

    public BaseCoin getBaseCoin ()
    {
        return baseCoin;
    }

    private Settings settings;

    public Settings settings ()
    {
        return settings;
    }

    public MetaBuilder meta (ItemStack itemStack)
    {
        return new MetaBuilder(this, itemStack);
    }

    private CreateCoin createCoin;

    public CreateCoin getCreateCoin ()
    {
        return createCoin;
    }

    private CoinUtil coinUtil;

    public CoinUtil getCoinUtil ()
    {
        return coinUtil;
    }

    private PickupHandler pickupHandler;

    public PickupHandler getPickupHandler ()
    {
        return pickupHandler;
    }

    private UnfairMobHandler unfairMobHandler;

    public UnfairMobHandler getUnfairMobHandler ()
    {
        return unfairMobHandler;
    }
}
