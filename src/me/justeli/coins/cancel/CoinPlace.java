package me.justeli.coins.cancel;

import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Eli on 2/4/2017.
 *
 */

public class CoinPlace implements Listener
{
    @EventHandler
    public void coinPlace (PlayerInteractEvent e)
    {
        if (!e.getAction().equals(Action.PHYSICAL)
                && e.getItem() != null
                && e.getItem().hasItemMeta()
                && e.getItem().getItemMeta()!= null
                && e.getItem().getItemMeta().hasDisplayName())
        {
            String pickupName = e.getItem().getItemMeta().getDisplayName();
            String coinName = ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin));

            if (pickupName.endsWith(coinName + Settings.hS.get(Config.STRING.multiSuffix)) && e.getPlayer().hasPermission("coins.withdraw"))
            {
                if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Container))
                {
                    e.setCancelled(true);
                    double amount = Integer.parseInt( ChatColor.stripColor(pickupName.split(" ")[0]) );
                    CoinsPickup.addMoney(e.getPlayer(), amount * e.getItem().getAmount(), 0);
                    e.getItem().setType(Material.AIR);
                }
            }
        }
    }

}
