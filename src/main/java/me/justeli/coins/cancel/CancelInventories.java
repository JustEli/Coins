package me.justeli.coins.cancel;

import io.papermc.lib.PaperLib;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
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
public class CancelInventories
        implements Listener
{
    @EventHandler
    public void avoidCraftingTable (CraftItemEvent e)
    {
        for (ItemStack stack : e.getInventory().getContents())
        {
            if (stack != null && stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName())
            {
                if (stack.getItemMeta().getDisplayName().contains(Settings.getCoinName()))
                {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void coinInventory (InventoryClickEvent e)
    {
        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds))
            if (e.getWhoClicked().getWorld().getName().equalsIgnoreCase(world))
                return;

        if (e.getWhoClicked() instanceof Player)
        {
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
            {
                String name = item.getItemMeta().getDisplayName();
                if (name.equals(Settings.getCoinName()))
                {
                    Player p = (Player) e.getWhoClicked();
                    e.setCancelled(true);
                    CoinsPickup.giveReward(item, p);

                    if (PaperLib.getMinecraftVersion() == 8)
                    {
                        // actually removes all items in the chest, but .setAmount(0) doesn't work for 1.8
                        e.getInventory().remove(item);
                        e.getClickedInventory().remove(item);
                    }
                    else
                    {
                        e.getCurrentItem().setAmount(0);
                    }
                }
            }
        }
    }
}
