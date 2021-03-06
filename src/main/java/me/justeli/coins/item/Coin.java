package me.justeli.coins.item;

import io.papermc.lib.PaperLib;
import me.justeli.coins.util.Skull;
import me.justeli.coins.util.Util;
import me.justeli.coins.config.Config;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

// todo
public class Coin
{
    private final ItemStack coin;

    public Coin ()
    {
        String texture = Config.skullTexture;
        this.coin = texture == null || texture.isEmpty()?
                new ItemStack(Config.coinItem())
                : Skull.of(texture);

        if (coin != null)
        {
            ItemMeta meta = this.coin.getItemMeta();
            if (meta != null)
            {
                meta.setDisplayName(Config.nameOfCoin());
                meta.setLore(new ArrayList<>());

                if (Config.customModelData > 0 && PaperLib.getMinecraftVersion() >= 14)
                {
                    meta.setCustomModelData(Config.customModelData);
                }

                if (Config.enchantedCoin)
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
        meta.setDisplayName(Util.color("&e" + amount + " &r" + Config.nameOfCoin() + Config.multiSuffix));
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
