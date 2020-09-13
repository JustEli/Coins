package me.justeli.coins.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

/**
 * Created by Eli on September 13, 2020.
 * Coins: me.justeli.coins.events
 */
public class PaperEvents implements Listener
{
    @EventHandler
    public void on (PlayerAttemptPickupItemEvent e)
    {
        PickupEvent event = new PickupEvent(e.getPlayer(), e.getItem(), true);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            e.setCancelled(true);
    }
}