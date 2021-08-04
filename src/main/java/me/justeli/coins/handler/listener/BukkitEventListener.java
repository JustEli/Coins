package me.justeli.coins.handler.listener;

import me.justeli.coins.event.PickupEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Created by Eli on September 13, 2020.
 * Coins: me.justeli.coins.events
 */
public class BukkitEventListener
        implements Listener
{
    @EventHandler (ignoreCancelled = true)
    public void bukkitPickupEvent (PlayerPickupItemEvent event)
    {
        PickupEvent registerEvent = new PickupEvent(event.getPlayer(), event.getItem());
        Bukkit.getServer().getPluginManager().callEvent(registerEvent);

        if (registerEvent.isCancelled())
        {
            event.setCancelled(true);
        }
    }
}