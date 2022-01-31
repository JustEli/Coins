package me.justeli.coins.handler;

import me.justeli.coins.config.Config;
import me.justeli.coins.item.CoinUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

/** by Eli on January 30, 2022 **/
public class ModificationHandler
        implements Listener
{
    @EventHandler
    public void avoidCrafting (CraftItemEvent event)
    {
        if (Config.ALLOW_MODIFICATION)
            return;

        for (ItemStack stack : event.getInventory().getContents())
        {
            if (!CoinUtil.isCoin(stack))
                continue;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void avoidCrafting (PrepareItemCraftEvent event)
    {
        if (Config.ALLOW_MODIFICATION)
            return;

        for (ItemStack stack : event.getInventory().getContents())
        {
            if (CoinUtil.isCoin(stack))
            {
                event.getInventory().setResult(null);
                break;
            }
        }
    }

    @EventHandler
    public void avoidAnvil (PrepareAnvilEvent event)
    {
        if (Config.ALLOW_NAME_CHANGE)
            return;

        if (event.getResult() != null && CoinUtil.isCoin(event.getResult()))
        {
            event.setResult(null);
        }
    }

    @EventHandler
    public void avoidFurnace (InventoryMoveItemEvent event)
    {
        if (Config.ALLOW_MODIFICATION)
            return;

        if (!(event.getDestination() instanceof FurnaceInventory))
            return;

        if (CoinUtil.isCoin(event.getItem()))
        {
            event.setCancelled(true);
        }
    }
}
