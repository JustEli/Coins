package me.justeli.coins.cancel;

import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class CancelHopper
        implements Listener
{
    @EventHandler (ignoreCancelled = true)
    public void itemHopper (InventoryPickupItemEvent e)
    {
        if (e.getInventory().getType() != InventoryType.HOPPER)
            return;

        ItemStack item = e.getItem().getItemStack();
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
        {
            String pickupName = item.getItemMeta().getDisplayName();
            String coinName = ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin));

            if (pickupName.equals(coinName))
            {
                if (Settings.hB.get(Config.BOOLEAN.disableHoppers)) e.setCancelled(true);
                else if (item.getItemMeta().hasLore()) e.getItem().setItemStack(new ItemStack(new Coin().item()));
            }
        }
    }
}
