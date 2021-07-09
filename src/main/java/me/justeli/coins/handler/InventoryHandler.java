package me.justeli.coins.handler;

import io.papermc.lib.PaperLib;
import me.justeli.coins.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Eli on 2 mei 2019.
 * spigotPlugins: me.justeli.coins.cancel
 */
public class InventoryHandler
        implements Listener
{
    @EventHandler
    public void avoidCraftingTable (CraftItemEvent event)
    {
        for (ItemStack stack : event.getInventory().getContents())
        {
            if (!Util.isDroppedCoin(stack) && !Util.isWithdrawnCoin(stack))
                continue;

            event.setCancelled(true);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void coinInventory (InventoryClickEvent event)
    {
        if (Util.isDisabledHere(event.getWhoClicked().getWorld()))
            return;

        if (!Util.isPlayer(event.getWhoClicked()))
            return;

        if (!Util.isDroppedCoin(event.getCurrentItem()))
            return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();

        PickupHandler.giveRandomMoney(item, player);

        if (PaperLib.getMinecraftVersion() == 8)
        {
            // actually removes all items in the chest, but .setAmount(0) doesn't work for 1.8
            event.getInventory().remove(item);
            event.getClickedInventory().remove(item);
        }
        else
        {
            event.getCurrentItem().setAmount(0);
        }
    }
}
