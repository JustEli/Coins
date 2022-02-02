package me.justeli.coins.handler;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class HopperHandler
        implements Listener
{
    private final Coins coins;

    public HopperHandler (Coins coins)
    {
        this.coins = coins;
    }

    @EventHandler (ignoreCancelled = true)
    public void itemHopper (InventoryPickupItemEvent event)
    {
        if (event.getInventory().getType() != InventoryType.HOPPER)
            return;

        ItemStack item = event.getItem().getItemStack();
        if (!this.coins.getCoinUtil().isDroppedCoin(item))
            return;

        if (Config.DISABLE_HOPPERS)
        {
            event.setCancelled(true);
        }
    }
}
