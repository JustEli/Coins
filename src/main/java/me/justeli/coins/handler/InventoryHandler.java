package me.justeli.coins.handler;

import me.justeli.coins.Coins;
import me.justeli.coins.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/* Eli @ May 2, 2019 (creation) */
public final class InventoryHandler
    implements Listener
{
    private final Coins coins;

    public InventoryHandler (Coins coins)
    {
        this.coins = coins;
    }

    @EventHandler (ignoreCancelled = true)
    public void onInventoryClick (InventoryClickEvent event)
    {
        if (Util.isDisabledHere(event.getWhoClicked().getWorld()))
            return;

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!this.coins.getCoinUtil().isDroppedCoin(event.getCurrentItem()))
            return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();

        this.coins.getPickupHandler().giveRandomMoney(item, player);
        if (event.getCurrentItem() == null)
            return;

        event.getCurrentItem().setAmount(0);
    }
}
