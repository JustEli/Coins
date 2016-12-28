package me.justeli.coins.events;

import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.settings.LoadSettings;
import me.justeli.coins.settings.Setting;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Eli on 12/22/2016.
 */
public class CoinDropEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final Player player;
    private boolean cancelled = false;
    private boolean stackable = LoadSettings.hB.get(Setting._Boolean.stackCoins);

    CoinDropEvent (Entity entity, Player player) {
        this.entity = entity;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean entityFromSpawner() {
        return PreventSpawner.fromSpawner(entity);
    }

    public EntityType getEntityType() {
        return entity.getType();
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Player getKiller() { return player; }

}
