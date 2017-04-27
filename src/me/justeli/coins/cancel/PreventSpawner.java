package me.justeli.coins.cancel;

import java.util.HashMap;

import me.justeli.coins.settings.Settings;
import me.justeli.coins.settings.Config;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class PreventSpawner implements Listener
{
	private final static HashMap<String, Boolean> spawner = new HashMap<>();

	@EventHandler
	public void preventSpawnerCoin (CreatureSpawnEvent e)
	{
        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds) )
            if (e.getEntity().getWorld().getName().equalsIgnoreCase(world))
                return;

		if (e.getSpawnReason().equals(SpawnReason.SPAWNER))
		{
			if (!Settings.hB.get(Config.BOOLEAN.spawnerDrop))
				spawner.put(e.getEntity().getUniqueId().toString() + ".spawner", true);
		}
	}

	public static boolean fromSpawner (Entity m)
	{
		String key = m.getUniqueId().toString() + ".spawner";
		return spawner.containsKey(key);
	}

	public static void removeFromList (Entity m)
	{
		String key = m.getUniqueId().toString() + ".spawner";
		spawner.remove(key);
	}



}
