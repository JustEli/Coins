package me.justeli.coins.handler.listener;

import me.justeli.coins.event.CoinPickupEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

/**
 * Created by Eli on September 13, 2020.
 * Coins: me.justeli.coins.events
 */
public class PaperPickupEvent
        implements Listener
{
    @EventHandler
    public void paperPickupEvent (PlayerAttemptPickupItemEvent event)
    {
        CoinPickupEvent registerEvent = new CoinPickupEvent(event.getPlayer(), event.getItem());
        Bukkit.getServer().getPluginManager().callEvent(registerEvent);

        if (registerEvent.isCancelled())
        {
            event.setCancelled(true);
        }
    }
}