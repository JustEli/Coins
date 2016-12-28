package me.justeli.coins.cancel;

import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.settings.LoadSettings;
import me.justeli.coins.settings.Setting;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CancelHopper implements Listener {
	
	@EventHandler (ignoreCancelled = true)
	public void coinInventory (InventoryClickEvent e) 
	{
        for (String world : LoadSettings.hA.get(Setting._Array.disabledWorlds) )
            if (e.getWhoClicked().getWorld().getName().equalsIgnoreCase(world))
                return;

		if (e.getWhoClicked() instanceof Player && (
				   e.getAction().equals(InventoryAction.PICKUP_ALL)
				|| e.getAction().equals(InventoryAction.PLACE_ALL)
				|| e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
			)) {
			
			ItemStack item = e.getCurrentItem();
			
			if (item != null)
				if (item.hasItemMeta())
					if (item.getItemMeta().getDisplayName() != null)
						if (item.getItemMeta().getDisplayName().equals(
								ChatColor.translateAlternateColorCodes('&', LoadSettings.hS.get(Setting._String.nameOfCoin))
						))
						{
							Player p = (Player)e.getWhoClicked();

							e.setCancelled(true);

                            e.getInventory().remove(item);
                            e.getClickedInventory().remove(item);

                            CoinsPickup.giveCoin(item, p);
							
						}

		}
	}

}
