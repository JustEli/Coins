package me.justeli.coins.events;

import me.justeli.coins.main.Load;
import me.justeli.coins.settings.Settings;
import me.justeli.coins.settings.Config;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import me.justeli.coins.api.ActionBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;

public class CoinsPickup implements Listener {

	private final static HashMap<String, Boolean> thrown = new HashMap<>();
	private static RegisteredServiceProvider<Economy> rep = Bukkit.getServicesManager().getRegistration(Economy.class);
	
	@EventHandler
	public void onPickup (PlayerPickupItemEvent e) {

		for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds) )
			if (e.getPlayer().getWorld().getName().equalsIgnoreCase(world))
				return;

		Item item = e.getItem();

		if (item.getItemStack().getItemMeta().hasDisplayName())
		{
			String pickupName = item.getItemStack().getItemMeta().getDisplayName();
			String coinName = ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin));

			if ( pickupName.equals(coinName) )
			{
				e.setCancelled(true);

				if (!e.getPlayer().hasPermission("coins.disable") || e.getPlayer().isOp())
					giveCoin(item, e.getPlayer(), 0);
			}

			else if ( pickupName.endsWith(coinName + "s") )
			{
				e.setCancelled(true);
				int amount = Integer.valueOf( ChatColor.stripColor(pickupName.split(" ")[0]) );
				if (!e.getPlayer().hasPermission("coins.disable") || e.getPlayer().isOp())
					giveCoin(item, e.getPlayer(), item.getItemStack().getAmount() * amount);
			}
		}

	}

	private static void giveCoin (Item item, Player p, long randomMoney)
	{
		ItemMeta meta = item.getItemStack().getItemMeta();

		if (meta.getLore() != null)
			if (thrown.containsKey(meta.getLore().get(0)))
				return;

		String random = String.valueOf(Math.random());
		meta.setLore(Collections.singletonList( random ));
		thrown.put(random, true);
		item.getItemStack().setItemMeta(meta);

		item.setVelocity(new Vector(0, 0.3, 0));

		new BukkitRunnable() {
			public void run() {

				this.cancel();

				item.remove();
				thrown.remove(meta.getLore().get(0));
				if (randomMoney == 0)
					giveReward(item.getItemStack(), p);
				else
					addMoney(p, randomMoney);

				if (Settings.hB.get(Config.BOOLEAN.pickupSound))
				{
					try
					{
						String sound = Settings.hS.get(Config.STRING.soundName);

						Sound playsound;
						if (Settings.hB.get(Config.BOOLEAN.olderServer) && sound.equals("BLOCK_LAVA_POP"))
							playsound = Sound.valueOf("NOTE_STICKS");
						else
							playsound = Sound.valueOf( sound.toUpperCase() );

						p.playSound(p.getEyeLocation(), playsound, 0.3f, 0.3f);
					}
					catch ( IllegalArgumentException e )
					{
						Bukkit.getLogger().severe( e.getMessage() + ": the sound does not exist. Change it in the Coins config." );
						Bukkit.getLogger().severe( "Please use a sound from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html" );
					}

				}


			}
		}.runTaskTimer(Load.main, 2, 0);

	}

	public static void giveReward (ItemStack item, Player p)
	{
		double second = Settings.hD.get(Config.DOUBLE.moneyAmount_from);
		double first = Settings.hD.get(Config.DOUBLE.moneyAmount_to) - second;

		int amount = item.getAmount();
		long total = amount * (long)( Math.random() * first + second );

		addMoney (p, total);
	}

	private static void addMoney (Player p, long amount)
	{
		rep.getProvider().depositPlayer(p, amount);
		new ActionBar( Settings.hS.get(Config.STRING.pickupMessage).replace("%amount%", String.valueOf(amount)) ).send(p);
	}

}
