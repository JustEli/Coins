package me.justeli.coins.handler.listener;

import me.justeli.coins.Coins;
import me.justeli.coins.event.PickupEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

/** Created by Eli on September 13, 2020. */
public class BukkitEventListener
        implements Listener
{
    private final Coins coins;

    public BukkitEventListener (Coins coins)
    {
        this.coins = coins;
    }

    @EventHandler (ignoreCancelled = true)
    public void bukkitPickupEvent (PlayerPickupItemEvent event)
    {
        PickupEvent registerEvent = new PickupEvent(event.getPlayer(), event.getItem());
        this.coins.getServer().getPluginManager().callEvent(registerEvent);

        if (registerEvent.isCancelled())
        {
            event.setCancelled(true);
        }
    }
}