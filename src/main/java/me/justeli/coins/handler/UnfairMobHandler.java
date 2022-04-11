package me.justeli.coins.handler;

import me.justeli.coins.Coins;
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
    public void onCreatureSpawn (CreatureSpawnEvent event)
    {
        if (event.getSpawnReason() == SpawnReason.SPAWNER || event.getEntityType() == EntityType.CAVE_SPIDER)
        {
            event.getEntity().getPersistentDataContainer().set(this.spawnerMob, PersistentDataType.INTEGER, 1);
        }
        else if (event.getSpawnReason() == SpawnReason.SLIME_SPLIT)
        {
            event.getEntity().getPersistentDataContainer().set(this.slimeSplit, PersistentDataType.INTEGER, 1);
        }
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
