package me.justeli.coins.handler;

import me.justeli.coins.config.Config;
import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class HopperHandler
        implements Listener
{
    @EventHandler (ignoreCancelled = true)
    public void itemHopper (InventoryPickupItemEvent event)
    {
        if (event.getInventory().getType() != InventoryType.HOPPER)
            return;

        ItemStack item = event.getItem().getItemStack();
        if (!CoinUtil.isDroppedCoin(item))
            return;

        if (Config.DISABLE_HOPPERS)
        {
            event.setCancelled(true);
        }
    }
}
