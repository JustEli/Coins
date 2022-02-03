package me.justeli.coins.handler;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import me.justeli.coins.util.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.persistence.PersistentDataType;

public final class UnfairMobHandler
        implements Listener
{
    private final NamespacedKey slimeSplit;
    private final NamespacedKey spawnerMob;

    public UnfairMobHandler (Coins coins)
    {
        this.slimeSplit = new NamespacedKey(coins, "coins-slime-split");
        this.spawnerMob = new NamespacedKey(coins, "coins-spawner-mob");
    }

    @EventHandler
    public void preventSpawnerCoin (CreatureSpawnEvent event)
    {
        if (Config.SPAWNER_DROP)
            return;

        if (Util.isDisabledHere(event.getEntity().getWorld()))
            return;

        if (event.getSpawnReason() != SpawnReason.SPAWNER && event.getEntityType() != EntityType.CAVE_SPIDER)
            return;

        event.getEntity().getPersistentDataContainer().set(this.spawnerMob, PersistentDataType.INTEGER, 1);
    }

    @EventHandler
    public void splitPrevent (CreatureSpawnEvent event)
    {
        if (event.getSpawnReason() != SpawnReason.SLIME_SPLIT || !Config.PREVENT_SPLITS)
            return;

        event.getEntity().getPersistentDataContainer().set(this.slimeSplit, PersistentDataType.INTEGER, 1);
    }

    public boolean fromSplit (Entity entity)
    {
        return entity.getPersistentDataContainer().has(this.slimeSplit, PersistentDataType.INTEGER);
    }

    public boolean fromSpawner (Entity entity)
    {
        return entity.getPersistentDataContainer().has(this.spawnerMob, PersistentDataType.INTEGER);
    }
}
