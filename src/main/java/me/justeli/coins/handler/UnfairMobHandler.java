package me.justeli.coins.handler;

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

public class UnfairMobHandler
        implements Listener
{
    private static final Set<UUID> SPAWNER_MOB = new HashSet<>();
    private static final Set<UUID> SLIME_SPLIT_MOB = new HashSet<>();

    @EventHandler
    public void preventSpawnerCoin (CreatureSpawnEvent event)
    {
        if (Config.SPAWNER_DROP)
            return;

        if (Util.isDisabledHere(event.getEntity().getWorld()))
            return;

        if (event.getSpawnReason() != SpawnReason.SPAWNER && event.getEntityType() != EntityType.CAVE_SPIDER)
            return;

        SPAWNER_MOB.add(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void splitPrevent (CreatureSpawnEvent event)
    {
        if (event.getSpawnReason() != SpawnReason.SLIME_SPLIT || !Config.PREVENT_SPLITS)
            return;

        SLIME_SPLIT_MOB.add(event.getEntity().getUniqueId());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void removeDeathEntity (EntityDeathEvent event)
    {
        removeFromList(event.getEntity());
    }

    public static boolean fromSplit (Entity entity)
    {
        return SLIME_SPLIT_MOB.contains(entity.getUniqueId());
    }

    public static boolean fromSpawner (Entity entity)
    {
        return SPAWNER_MOB.contains(entity.getUniqueId());
    }

    public static void removeFromList (Entity entity)
    {
        SPAWNER_MOB.remove(entity.getUniqueId());
        SLIME_SPLIT_MOB.remove(entity.getUniqueId());
    }
}
