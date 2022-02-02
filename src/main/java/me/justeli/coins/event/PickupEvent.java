package me.justeli.coins.event;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Created by Eli on September 13, 2020. */
public final class PickupEvent
        extends Event
        implements Cancellable
{
    private final Player player;
    private final Item item;

    public PickupEvent (Player player, Item item)
    {
        this.player = player;
        this.item = item;
    }

    public Player getPlayer ()
    {
        return player;
    }

    public Item getItem ()
    {
        return item;
    }

    // -- Cancellable --

    private boolean cancelled;

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

    // -- HandlerList --

    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    public HandlerList getHandlers ()
    {
        return HANDLERS;
    }

    public static HandlerList getHandlerList ()
    {
        return HANDLERS;
    }
}