package me.justeli.coins.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Created by Eli on September 13, 2020.
 * Coins: me.justeli.coins.events
 */
public class BukkitEvents implements Listener
{
    @EventHandler (ignoreCancelled = true)
    public void on (PlayerPickupItemEvent e)
    {
        PickupEvent event = new PickupEvent(e.getPlayer(), e.getItem(), false);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            e.setCancelled(true);
    }
}