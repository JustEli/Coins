package me.justeli.coins.handler;

import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Eli on 2 mei 2019.
 * spigotPlugins: me.justeli.coins.cancel
 */
public class InventoryHandler
        implements Listener
{
    @EventHandler (ignoreCancelled = true)
    public void coinInventory (InventoryClickEvent event)
    {
        if (Util.isDisabledHere(event.getWhoClicked().getWorld()))
            return;

        if (!Util.isPlayer(event.getWhoClicked()))
            return;

        if (!CoinUtil.isDroppedCoin(event.getCurrentItem()))
            return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        PickupHandler.giveRandomMoney(item, player);
        event.getCurrentItem().setAmount(0);
    }
}
