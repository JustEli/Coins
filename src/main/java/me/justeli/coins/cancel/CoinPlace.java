package me.justeli.coins.cancel;

import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Eli on 2/4/2017.
 */

public class CoinPlace
        implements Listener
{
    @EventHandler
    public void coinPlace (PlayerInteractEvent e)
    {
        if (!e.getAction().equals(Action.PHYSICAL)
                && e.getItem() != null
                && e.getItem().hasItemMeta()
                && e.getItem().getItemMeta() != null
                && e.getItem().getItemMeta().hasDisplayName())
        {
            Player p = e.getPlayer();

            String pickupName = e.getItem().getItemMeta().getDisplayName();
            String coinName = ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin));

            if (pickupName.endsWith(coinName + Settings.hS.get(Config.STRING.multiSuffix)))
            {
                if (Settings.hB.get(Config.BOOLEAN.olderServer) || !p.hasPermission("coins.withdraw"))
                {
                    e.setCancelled(true);
                    return;
                }

                if (e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Container))
                {
                    e.setCancelled(true);
                    int multi = e.getItem().getAmount();
                    e.getItem().setAmount(0);

                    double amount = Integer.parseInt(ChatColor.stripColor(pickupName.split(" ")[0]));
                    CoinsPickup.addMoney(p, amount * multi, 0);
                }
            }
        }
    }

}
