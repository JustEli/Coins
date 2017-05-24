package me.justeli.coins.main;

import me.justeli.coins.cancel.CancelHopper;
import me.justeli.coins.cancel.CoinPlace;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.events.DropCoin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Eli on 12/13/2016.
 *
 */

public class Coins extends JavaPlugin
{
    public static Coins main;
    private static Economy eco;

    @Override
    public void onEnable ()
    {
        main = this;

        registerConfig();
        registerEvents();
        registerCommands();

        String v = Bukkit.getVersion();
        if (v.contains("1.8") || v.contains("1.7"))
            Settings.hB.put(Config.BOOLEAN.olderServer, true);

        if (getServer().getPluginManager().getPlugin("Vault") == null)
            Bukkit.getPluginManager().disablePlugin(this);

        try
        {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            eco = rsp.getProvider();
        }
        catch (NullPointerException | NoClassDefFoundError e)
        {
            Settings.errorMessage(Settings.Msg.NO_ECONOMY_SUPPORT, null);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Metrics metrics = new Metrics(this);

        metrics.addCustomChart(new Metrics.SimplePie("language")
        { @Override public String getValue() { return WordUtils.capitalize(Settings.getLanguage()); } });

        metrics.addCustomChart(new Metrics.SimplePie("currencySymbol")
        { @Override public String getValue() { return Settings.hS.get(Config.STRING.currencySymbol); } });

        metrics.addCustomChart(new Metrics.SimplePie("moneyAmount")
        { @Override public String getValue() { return ( String.valueOf((Settings.hD.get(Config.DOUBLE.moneyAmount_from) + Settings.hD.get(Config.DOUBLE.moneyAmount_to))/2) ); } });

        metrics.addCustomChart(new Metrics.SimplePie("dropChance")
        { @Override public String getValue() { return Settings.hD.get(Config.DOUBLE.dropChance)*100 + "%"; } });

        metrics.addCustomChart(new Metrics.SimplePie("pickupSound")
        { @Override public String getValue() { return Settings.hS.get(Config.STRING.soundName); } });

        metrics.addCustomChart(new Metrics.SimplePie("enableWithdraw")
        { @Override public String getValue() { return String.valueOf(Settings.hB.get(Config.BOOLEAN.enableWithdraw)); } });

        metrics.addCustomChart(new Metrics.SimplePie("loseOnDeath")
        { @Override public String getValue() { return String.valueOf(Settings.hB.get(Config.BOOLEAN.loseOnDeath)); } });

        metrics.addCustomChart(new Metrics.SimplePie("passiveDrop")
        { @Override public String getValue() { return String.valueOf(Settings.hB.get(Config.BOOLEAN.passiveDrop)); } });
    }

    public static Economy getEcononomy ()
    {
        return eco;
    }

    private void registerEvents ()
    {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new CancelHopper(), this);
        manager.registerEvents(new PreventSpawner(), this);
        manager.registerEvents(new CoinsPickup(), this);
        manager.registerEvents(new DropCoin(), this);
        manager.registerEvents(new CoinPlace(), this);
    }

    private void registerCommands ()
    {
        this.getCommand("coins").setExecutor(new Cmds());
        this.getCommand("withdraw").setExecutor(new Cmds());
    }

    private void registerConfig ()
    {
        Settings.enums();
    }

}
