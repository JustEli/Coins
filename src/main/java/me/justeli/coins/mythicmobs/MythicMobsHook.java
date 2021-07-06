package me.justeli.coins.mythicmobs;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitItemStack;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.drops.droppables.ItemDrop;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import me.justeli.coins.item.Coin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Logger;

public class MythicMobsHook implements Listener {

	private Logger log;

	public void Enable(Plugin plugin) {
		log = Bukkit.getLogger();
		Bukkit.getPluginManager().registerEvents(this, plugin);

		log.info("[Coins] MythicMobs API Hook Enabled!");
	}
	public static boolean isMythicMob(Entity entity){
		return new BukkitAPIHelper().isMythicMob(entity);
	}
	public static boolean isMythicMob(UUID u){
		return new BukkitAPIHelper().isMythicMob(u);
	}

	public void Disable(Plugin plugin){
		HandlerList.unregisterAll(this);

		log.info("[Coins] MythicMobs API Hook Disabled!");
	}


	/*
	 * Registers all of the custom drops when MythicDropLoadEvent is called
	 */
	@EventHandler
	public void onMythicDropLoad(MythicDropLoadEvent event)	{
		log.info("[Coins] MythicDropLoadEvent called for drop " + event.getDropName());

		if(event.getDropName().equalsIgnoreCase("COINS"))	{
			String line = event.getConfig().getLine();
			MythicLineConfig config = event.getConfig();
			BukkitItemStack coin = new BukkitItemStack(new Coin().item());
			//org.bukkit.inventory.
			ItemDrop drop = new ItemDrop(line, config, coin);


			//Drop drop = new CoinItem(event.getConfig());
			event.register(drop);
			log.info("[Coins] -- Registered COINS drop!");
		}
	}
}
