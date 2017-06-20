package me.justeli.coins.item;

import me.justeli.coins.main.Coins;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collections;

public class CoinParticles
{
	public static void dropCoins (Location location, int radius, int amount)
	{
		Location l = location.add(0.0, 0.5, 0.0);
		ItemStack coin = new Coin().unique().item();
		ItemMeta meta = coin.getItemMeta();

		for (int i = 0; i < amount; i++)
		{
			later(i, () ->
			{
				meta.setLore(Collections.singletonList( String.valueOf(Math.random()) ));
				coin.setItemMeta(meta);
				Item item = l.getWorld().dropItem(l, coin);
				item.setPickupDelay(30);
				item.setVelocity(new Vector(
						( Math.random() - 0.5 ) * radius/10,
						Math.random() * radius/5,
						( Math.random() - 0.5 ) * radius/10
				));
			});
		}
	}

	private static void later (int ticks, Runnable runnable)
	{
		BukkitTask task = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				runnable.run();
			}
		}
				.runTaskLater(Coins.getInstance(), ticks);

		return task.getTaskId();
	}

}
