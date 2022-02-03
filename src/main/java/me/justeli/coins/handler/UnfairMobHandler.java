package me.justeli.coins.handler;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import me.justeli.coins.util.Util;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class UnfairMobHandler
        implements Listener
{
    private final Coins coins;

    public UnfairMobHandler (Coins coins)
    {
        this.coins = coins;
    }

    private final Set<UUID> spawnerMobCache = new HashSet<>();
    private final Set<UUID> slimeSplitMobCache = new HashSet<>();

    @EventHandler
    public void preventSpawnerCoin (CreatureSpawnEvent event)
    {
        if (Config.SPAWNER_DROP)
            return;

        if (Util.isDisabledHere(event.getEntity().getWorld()))
            return;

        if (event.getSpawnReason() != SpawnReason.SPAWNER && event.getEntityType() != EntityType.CAVE_SPIDER)
            return;

        spawnerMobCache.add(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void splitPrevent (CreatureSpawnEvent event)
    {
        if (event.getSpawnReason() != SpawnReason.SLIME_SPLIT || !Config.PREVENT_SPLITS)
            return;

        slimeSplitMobCache.add(event.getEntity().getUniqueId());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void removeDeathEntity (EntityDeathEvent event)
    {
        removeFromList(event.getEntity());
    }

    public boolean fromSplit (Entity entity)
    {
        return slimeSplitMobCache.contains(entity.getUniqueId());
    }

    public boolean fromSpawner (Entity entity)
    {
        return spawnerMobCache.contains(entity.getUniqueId());
    }

    public void removeFromList (Entity entity)
    {
        spawnerMobCache.remove(entity.getUniqueId());
        slimeSplitMobCache.remove(entity.getUniqueId());
    }
}
