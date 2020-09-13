package me.justeli.coins.events;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Eli on September 13, 2020.
 * Coins: me.justeli.coins.events
 */
public class PickupEvent
        extends Event
        implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Item item;
    private boolean cancelled;
    private boolean viaPaper;

    public PickupEvent (Player player, Item item, boolean viaPaper)
    {
        this.player = player;
        this.item = item;
        this.viaPaper = viaPaper;
    }

    public Player getPlayer ()
    {
        return player;
    }

    public Item getItem ()
    {
        return item;
    }

    public HandlerList getHandlers ()
    {
        return handlers;
    }

    public static HandlerList getHandlerList ()
    {
        return handlers;
    }

    public boolean isViaPaper ()
    {
        return viaPaper;
    }

    @Override
    public boolean isCancelled ()
    {
        return cancelled;
    }

    @Override
    public void setCancelled (boolean cancel)
    {
        cancelled = cancel;
    }
}