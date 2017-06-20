package me.justeli.coins.events;

import me.justeli.coins.api.ActionBar;
import me.justeli.coins.main.Coins;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class CoinsPickup implements Listener
{
	private final static HashMap<String, Boolean> thrown = new HashMap<>();
	private final static HashMap<UUID, Double> pickup = new HashMap<>();
	
	@EventHandler (ignoreCancelled = true)
	public void onPickup (PlayerPickupItemEvent e)
	{
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

				if (!e.getPlayer().hasPermission("coins.disable") || e.getPlayer().isOp() || e.getPlayer().hasPermission("*"))
					giveCoin(item, e.getPlayer(), 0);
			}

			else if ( pickupName.endsWith(coinName + "s") )
			{
				e.setCancelled(true);
				int amount = Integer.valueOf( ChatColor.stripColor(pickupName.split(" ")[0]) );
				if (!e.getPlayer().hasPermission("coins.disable") || e.getPlayer().isOp() || e.getPlayer().hasPermission("*"))
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

		new BukkitRunnable()
		{
			public void run()
			{
				this.cancel();

				item.remove();
				thrown.remove(meta.getLore().get(0));
				if (randomMoney == 0)
					giveReward(item.getItemStack(), p);
				else
					addMoney(p, randomMoney, 0);

				if (Settings.hB.get(Config.BOOLEAN.pickupSound))
				{
					try
					{
						String sound = Settings.hS.get(Config.STRING.soundName);

						Sound playsound = Sound.valueOf( Settings.hB.get(Config.BOOLEAN.olderServer)
										&& ( sound.equals("BLOCK_LAVA_POP") || sound.equals("ITEM_ARMOR_EQUIP_GOLD"))?
										"NOTE_STICKS" : sound.toUpperCase());

						p.playSound(p.getEyeLocation(), playsound, 0.3f, 0.3f);
					}
					catch ( IllegalArgumentException e )
					{ Settings.errorMessage(Settings.Msg.NO_SUCH_SOUND, new String[] {e.getMessage()} ); }

				}

			}
		}.runTaskTimer(Coins.getInstance(), 2, 0);

	}

	public static void giveReward (ItemStack item, Player p)
	{
		double second = Settings.hD.get(Config.DOUBLE.moneyAmount_from);
		double first = Settings.hD.get(Config.DOUBLE.moneyAmount_to) - second;

		int amount = item.getAmount();
		double total = amount * ( Math.random() * first + second );

		addMoney (p, total, Settings.hD.get(Config.DOUBLE.moneyDecimals).intValue());
	}

	private static void addMoney (Player p, double a, int integer)
	{
		final double amount = format(a, integer);
		Coins.getEconomy().depositPlayer(p, amount);

		final UUID u = p.getUniqueId();

		pickup.put(u, amount + (pickup.containsKey(u)? format(pickup.get(u), integer) : 0));
		final double newAmount = format(pickup.get(u), integer);

		Runnable task = () ->
		{
			if (pickup.containsKey(u) && format(pickup.get(u), integer) == newAmount)
				pickup.remove(u);
		};
		Bukkit.getScheduler().runTaskLater(Coins.getInstance(), task, 10L);

		new ActionBar( Settings.hS.get(Config.STRING.pickupMessage)
				.replace("%amount%", String.format("%." + integer + "f", newAmount) )
				.replace("{$}", Settings.hS.get(Config.STRING.currencySymbol))).send(p);
	}

	private static Double format (Double amount, int decimals)
	{
		return Double.parseDouble(String.format("%." + decimals + "f", amount));
	}

}
