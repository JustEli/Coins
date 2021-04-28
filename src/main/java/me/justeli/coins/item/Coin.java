package me.justeli.coins.item;

import me.justeli.coins.api.SkullValue;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class Coin
{
    private final ItemStack coin;

    public Coin ()
    {
        String texture = Settings.hS.get(Config.STRING.skullTexture);
        this.coin = texture == null || texture.isEmpty()?
                new ItemStack(Material.valueOf(Settings.hS.get(Config.STRING.coinItem)))
                : SkullValue.get(texture);

        if (coin != null)
        {
            ItemMeta meta = this.coin.getItemMeta();
            if (meta != null)
            {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Settings.hS.get(Config.STRING.nameOfCoin)));
                meta.setLore(new ArrayList<>());

                if (Settings.hD.get(Config.DOUBLE.customModelData) > 0)
                {
                    meta.setCustomModelData(Settings.hD.get(Config.DOUBLE.customModelData).intValue());
                }

                if (Settings.hB.get(Config.BOOLEAN.enchantedCoin))
                {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                }
            }
            this.coin.setItemMeta(meta);
        }
    }

    /**
     * Sets the coin to be NOT stackable.
     */
    public Coin unique ()
    {
        ItemMeta meta = this.coin.getItemMeta();
        if (meta != null)
            meta.setLore(Collections.singletonList(UUID.randomUUID().toString()));
        this.coin.setItemMeta(meta);

        return this;
    }

    /**
     * Makes it a withdrawn item.
     *
     * @param amount amount of coins
     *
     * @return the Coin
     */

    public Coin withdraw (long amount)
    {
        ItemMeta meta = this.coin.getItemMeta();
        if (meta != null)
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + amount + " &r" +
                    Settings.hS.get(Config.STRING.nameOfCoin) + Settings.hS.get(Config.STRING.multiSuffix)));
        this.coin.setItemMeta(meta);

        return this;
    }

    public Coin stack (boolean stack)
    {
        return stack? this : unique();
    }

    /**
     * @return the ItemStack itself.
     */
    public ItemStack item ()
    {
        return this.coin;
    }

}
