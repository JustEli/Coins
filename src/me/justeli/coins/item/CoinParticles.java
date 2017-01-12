package me.justeli.coins.item;

import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class CoinParticles {

	public static void dropCoins (Player player, int radius, int amount)
	{
		Location l = player.getLocation().add(0.0, 0.5, 0.0);
		ItemStack coin = new Coin().unique().item();
		ItemMeta meta = coin.getItemMeta();

		for (int i = 0; i < amount; i++)
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
		}
	}

}
