package me.justeli.coins.main;

import me.justeli.coins.cancel.CancelHopper;
import me.justeli.coins.cancel.CoinPlace;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.events.DropCoin;
import me.justeli.coins.item.CoinParticles;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Eli on 12/13/2016.
 *
 */

public class Coins extends JavaPlugin
{
    private static Coins main;
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

        metrics.add("language", WordUtils.capitalize(Settings.getLanguage()));
        metrics.add("currencySymbol", Settings.hS.get(Config.STRING.currencySymbol));
        metrics.add("dropChance", Settings.hD.get(Config.DOUBLE.dropChance)*100 + "%");
        metrics.add("pickupSound", Settings.hS.get(Config.STRING.soundName));
        metrics.add("enableWithdraw", String.valueOf(Settings.hB.get(Config.BOOLEAN.enableWithdraw)));
        metrics.add("loseOnDeath", String.valueOf(Settings.hB.get(Config.BOOLEAN.loseOnDeath)));
        metrics.add("passiveDrop", String.valueOf(Settings.hB.get(Config.BOOLEAN.passiveDrop)));

        metrics.add("nameOfCoin", Settings.hS.get(Config.STRING.nameOfCoin));
        metrics.add("pickupMessage", Settings.hS.get(Config.STRING.pickupMessage));
        metrics.add("moneyDecimals", String.valueOf(Settings.hD.get(Config.DOUBLE.moneyDecimals).intValue()));
        metrics.add("stackCoins", String.valueOf(Settings.hB.get(Config.BOOLEAN.stackCoins)));
        metrics.add("playerDrop", String.valueOf(Settings.hB.get(Config.BOOLEAN.playerDrop)));
        metrics.add("spawnerDrop", String.valueOf(Settings.hB.get(Config.BOOLEAN.spawnerDrop)));

        metrics.add("moneyAmount", ( String.valueOf((Settings.hD.get(Config.DOUBLE.moneyAmount_from)
                + Settings.hD.get(Config.DOUBLE.moneyAmount_to))/2) ));
    }

    public static Economy getEconomy ()
    {
        return eco;
    }

    public static void particles (Location location, int radius, int amount)
    {
        CoinParticles.dropCoins(location, radius, amount);
    }

    public static Coins getInstance ()
    {
        return main;
    }

    public static boolean mobFromSpawner (Entity entity)
    {
        return PreventSpawner.fromSpawner(entity);
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
        if (Settings.hB.get(Config.BOOLEAN.enableWithdraw))
            this.getCommand("withdraw").setExecutor(new Cmds());
    }

    private void registerConfig ()
    {
        Settings.enums();
    }

}
