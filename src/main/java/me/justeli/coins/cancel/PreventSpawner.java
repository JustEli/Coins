package me.justeli.coins.cancel;

import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.HashMap;

public class PreventSpawner
        implements Listener
{
    private final static HashMap<String, Boolean> prevent = new HashMap<>();

    @EventHandler
    public void preventSpawnerCoin (CreatureSpawnEvent e)
    {
        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds))
            if (e.getEntity().getWorld().getName().equalsIgnoreCase(world))
                return;

        if (e.getSpawnReason().equals(SpawnReason.SPAWNER) || e.getEntityType().equals(EntityType.CAVE_SPIDER))
        {
            if (!Settings.hB.get(Config.BOOLEAN.spawnerDrop))
                prevent.put(e.getEntity().getUniqueId().toString() + ".spawner", true);
        }
    }

    @EventHandler
    public void splitPrevent (CreatureSpawnEvent e)
    {
        if (e.getSpawnReason().equals(SpawnReason.SLIME_SPLIT))
            if (Settings.hB.get(Config.BOOLEAN.preventSplits))
                prevent.put(e.getEntity().getUniqueId().toString() + ".slime", true);
    }

    public static boolean fromSplit (Entity m)
    {
        String key = m.getUniqueId().toString() + ".slime";
        return prevent.containsKey(key);
    }

    public static boolean fromSpawner (Entity m)
    {
        String key = m.getUniqueId().toString() + ".spawner";
        return prevent.containsKey(key);
    }

    public static void removeFromList (Entity m)
    {
        String key = m.getUniqueId().toString();
        prevent.remove(key + ".spawner");
        prevent.remove(key + ".slime");
    }
}
