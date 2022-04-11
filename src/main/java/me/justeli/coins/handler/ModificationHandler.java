package me.justeli.coins.handler;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

/** by Eli on January 30, 2022 **/
public final class ModificationHandler
        implements Listener
{
    private final Coins coins;

    public ModificationHandler (Coins coins)
    {
        this.coins = coins;
    }

    @EventHandler
    public void onCraftItem (CraftItemEvent event)
    {
        if (Config.ALLOW_MODIFICATION)
            return;

        for (ItemStack stack : event.getInventory().getContents())
        {
            if (!this.coins.getCoinUtil().isCoin(stack))
                continue;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareItemCraft (PrepareItemCraftEvent event)
    {
        if (Config.ALLOW_MODIFICATION)
            return;

        for (ItemStack stack : event.getInventory().getContents())
        {
            if (this.coins.getCoinUtil().isCoin(stack))
            {
                event.getInventory().setResult(null);
                break;
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil (PrepareAnvilEvent event)
    {
        if (Config.ALLOW_NAME_CHANGE)
            return;

        if (event.getResult() != null && this.coins.getCoinUtil().isCoin(event.getResult()))
        {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onFurnaceSmelt (FurnaceSmeltEvent event)
    {
        if (Config.ALLOW_MODIFICATION)
            return;

        if (this.coins.getCoinUtil().isCoin(event.getSource()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFurnaceBurn (FurnaceBurnEvent event)
    {
        if (Config.ALLOW_MODIFICATION)
            return;

        if (this.coins.getCoinUtil().isCoin(event.getFuel()))
        {
            event.setBurnTime(0);
            event.setBurning(false);
        }
    }
}
