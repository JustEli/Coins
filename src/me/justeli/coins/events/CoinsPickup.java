package me.justeli.coins.events;

import me.justeli.coins.settings.LoadSettings;
import me.justeli.coins.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import me.justeli.coins.api.ActionBar;

public class CoinsPickup implements Listener {
	
	private static RegisteredServiceProvider<Economy> rep = Bukkit.getServicesManager().getRegistration(Economy.class);
	
	@EventHandler
	public void onPickup (PlayerPickupItemEvent e) {

		for (String world : LoadSettings.hA.get(Setting._Array.disabledWorlds) )
			if (e.getPlayer().getWorld().getName().equalsIgnoreCase(world))
				return;

		{
			ItemStack item = e.getItem().getItemStack();

			if (item.getItemMeta().getDisplayName() != null)
				if (item.getItemMeta().getDisplayName().equals(
						ChatColor.translateAlternateColorCodes('&', LoadSettings.hS.get(Setting._String.nameOfCoin) )
                )) {

                    e.setCancelled(true);
                    if (!e.getPlayer().hasPermission("coins.disable") || e.getPlayer().isOp())
                    {
                        e.getItem().remove();
                        giveCoin(item, e.getPlayer());
                    }

				}
		}
		
	}

	public static void giveCoin (ItemStack item, Player p)
	{
		double second = LoadSettings.hD.get(Setting._Double.moneyAmount_from);
		double first = LoadSettings.hD.get(Setting._Double.moneyAmount_to) - second;

		int amount = item.getAmount();
		long total = amount * (long)( Math.random() * first + second );

		rep.getProvider().depositPlayer(p, total);
		new ActionBar( LoadSettings.hS.get(Setting._String.pickupMessage).replace("%amount%", String.valueOf(total)) ).send(p);

		if (LoadSettings.hB.get(Setting._Boolean.pickupSound))
		{
            String sound = LoadSettings.hS.get(Setting._String.soundName);

            if (LoadSettings.hB.get(Setting._Boolean.olderServer) && sound.equals("BLOCK_LAVA_POP"))
                p.playSound(p.getEyeLocation(), Sound.valueOf("NOTE_STICKS"), 0.1f, 0.1f);
            else
                p.playSound(p.getEyeLocation(), Sound.valueOf( sound ), 0.3f, 0.3f);
        }

	}

}
