package me.justeli.coins.item;

import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class Coin
{
    private ItemStack coin;
    public Coin ()
    {
        this.coin = new ItemStack(Material.DOUBLE_PLANT);
        ItemMeta meta = this.coin.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin)));
        this.coin.setItemMeta(meta);
    }

    /**
     * Sets the coin to be NOT stackable.
     */
    public Coin unique ()
    {
        ItemMeta meta = this.coin.getItemMeta();
        meta.setLore(Collections.singletonList( String.valueOf(Math.random()) ));
        this.coin.setItemMeta(meta);

        return this;
    }

    /**
     * Makes it a withdrawn item.
     * @param amount amount of coins
     * @return the Coin
     */

    public Coin withdraw (long amount)
    {
        ItemMeta meta = this.coin.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + amount + " &r" + Settings.hS.get(Config.STRING.nameOfCoin) + "s"));
        this.coin.setItemMeta(meta);

        return this;
    }

    public Coin stack (boolean stack)
    {
        return stack ? this : unique();
    }

    /**
     * @return the ItemStack itself.
     */
    public ItemStack item ()
    {
        return this.coin;
    }

}
