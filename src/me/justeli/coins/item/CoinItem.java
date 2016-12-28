package me.justeli.coins.item;

import me.justeli.coins.settings.LoadSettings;
import me.justeli.coins.settings.Setting;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

import java.util.Collections;

public class CoinItem {

	public static ItemStack sunflower(boolean stackable)
    {
        ItemStack coin = new ItemStack(Material.DOUBLE_PLANT);
        ItemMeta meta = coin.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LoadSettings.hS.get(Setting._String.nameOfCoin)));
        if (!stackable)
            meta.setLore(Collections.singletonList( String.valueOf(Math.random()) ));
        coin.setItemMeta(meta);

        return coin;
    }

    @Deprecated
    public static ItemStack sunflower()
    {
        ItemStack coin = new ItemStack(Material.DOUBLE_PLANT);
        ItemMeta meta = coin.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LoadSettings.hS.get(Setting._String.nameOfCoin)));
        if (!LoadSettings.hB.get(Setting._Boolean.stackCoins))
            meta.setLore(Collections.singletonList( String.valueOf(Math.random()) ));
        coin.setItemMeta(meta);

        return coin;
    }

}
