package me.justeli.coins.handler.listener;

import me.justeli.coins.Coins;
import me.justeli.coins.event.PickupEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

/* Eli @ September 13, 2020 (creation) */
public final class PaperEventListener
    implements Listener
{
    private final Coins coins;

    public PaperEventListener (Coins coins)
    {
        this.coins = coins;
    }

    @EventHandler
    public void onPickupPaper (PlayerAttemptPickupItemEvent event)
    {
        PickupEvent registerEvent = new PickupEvent(event.getPlayer(), event.getItem());
        this.coins.getServer().getPluginManager().callEvent(registerEvent);

        if (registerEvent.isCancelled())
        {
            event.setCancelled(true);
        }
    }
}
