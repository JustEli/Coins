package me.justeli.coins.cancel;

import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.settings.Settings;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

public class CancelHopper implements Listener
{
	@EventHandler (ignoreCancelled = true)
	public void itemHopper (InventoryPickupItemEvent e)
	{
		if (e.getInventory().getType().equals(InventoryType.HOPPER))
		{
			ItemStack item = e.getItem().getItemStack();
			if (item.getItemMeta().hasDisplayName() && item.getItemMeta().hasLore())
			{
				String pickupName = item.getItemMeta().getDisplayName();
				String coinName = ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin));

				if ( pickupName.equals(coinName) )
					e.getItem().setItemStack(new ItemStack(new Coin().item()));

			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void coinInventory (InventoryClickEvent e) 
	{
        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds) )
            if (e.getWhoClicked().getWorld().getName().equalsIgnoreCase(world))
                return;

		if (e.getWhoClicked() instanceof Player && (
				   e.getAction().equals(InventoryAction.PICKUP_ALL)
				|| e.getAction().equals(InventoryAction.PLACE_ALL)
				|| e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
			))
		{
			ItemStack item = e.getCurrentItem();
			
			if (item != null)
				if (item.hasItemMeta())
					if (item.getItemMeta().hasDisplayName())
						if (item.getItemMeta().getDisplayName().equals(
								ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin))
						))
						{
							Player p = (Player)e.getWhoClicked();

							e.setCancelled(true);

                            e.getInventory().remove(item);
                            e.getClickedInventory().remove(item);

                            CoinsPickup.giveReward(item, p);
							
						}

		}
	}

}
