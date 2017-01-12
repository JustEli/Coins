package me.justeli.coins.main;

import me.justeli.coins.cancel.CancelHopper;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.events.DropCoin;
import me.justeli.coins.settings.Settings;
import me.justeli.coins.settings.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Eli on 12/13/2016.
 */

public class Load extends JavaPlugin
{
    public static Load main;

    @Override
    public void onEnable ()
    {
        main = this;

        registerConfig();
        registerEvents();
        registerCommands();

        if (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.7"))
            Settings.hB.put(Config.BOOLEAN.olderServer, true);

    }

    private void registerEvents ()
    {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new CancelHopper(), this);
        manager.registerEvents(new PreventSpawner(), this);
        manager.registerEvents(new CoinsPickup(), this);
        manager.registerEvents(new DropCoin(), this);
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
