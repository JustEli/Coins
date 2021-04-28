package me.justeli.coins.cancel;

import me.justeli.coins.api.Util;
import me.justeli.coins.events.CoinsPickup;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
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
                    e.getCurrentItem().setAmount(0);
                }
            }
        }
    }
    // 2 copies of the one above here for creative inventories

    @EventHandler (ignoreCancelled = true)
    public void onMiddleClick (InventoryClickEvent e)
    {
        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds))
            if (e.getWhoClicked().getWorld().getName().equalsIgnoreCase(world))
                return;

        if (e.getWhoClicked() instanceof Player)
            if (!e.getWhoClicked().hasPermission("coins.creative") && e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE))
                removeCreativeCoins(e, e.getCurrentItem());
    }

    @EventHandler (ignoreCancelled = true)
    public void onMiddleClick2 (InventoryCreativeEvent e)
    {
        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds))
            if (e.getWhoClicked().getWorld().getName().equalsIgnoreCase(world))
                return;

        if (e.getWhoClicked() instanceof Player)
            if (!e.getWhoClicked().hasPermission("coins.creative"))
                removeCreativeCoins(e, e.getCursor());

    }

    private void removeCreativeCoins (InventoryClickEvent e, ItemStack item)
    {
        if (item != null && item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
        {
            String name = item.getItemMeta().getDisplayName();
            if (name.contains(Settings.getCoinName()))
            {
                e.setCancelled(true);

                e.getInventory().remove(item);
                e.setCurrentItem(new ItemStack(Material.AIR));

                if (e.getClickedInventory() != null)
                    e.getClickedInventory().remove(item);
            }
        }
    }
}
