package me.justeli.coins.handler.listener;

import me.justeli.coins.Coins;
import me.justeli.coins.event.PickupEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

/* Eli @ September 13, 2020 (creation) */
public final class BukkitEventListener
    implements Listener
{
    private final Coins coins;

    public BukkitEventListener (Coins coins)
    {
        this.coins = coins;
    }

    @EventHandler (ignoreCancelled = true)
    public void onPickupBukkit (EntityPickupItemEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;

        PickupEvent registerEvent = new PickupEvent((Player) event.getEntity(), event.getItem());
        this.coins.getServer().getPluginManager().callEvent(registerEvent);

        if (registerEvent.isCancelled())
        {
            event.setCancelled(true);
        }
    }
}
