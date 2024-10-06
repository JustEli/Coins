package me.justeli.coins.item;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import me.justeli.coins.util.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/* Eli @ January 30, 2022 (creation) **/
public final class BaseCoin
{
    private final MetaBuilder withdrawnCoin;
    private final MetaBuilder droppedCoin;
    private final MetaBuilder otherCoin;

    public BaseCoin (Coins coins)
    {
        String texture = Config.SKULL_TEXTURE;

        ItemStack baseCoin = texture == null || texture.isEmpty()? new ItemStack(Config.COIN_ITEM) : Skull.of(texture);
        ItemMeta baseCoinMeta = baseCoin.getItemMeta();

        if (baseCoinMeta != null) {
            if (Config.CUSTOM_MODEL_DATA > 0)
            {
                baseCoinMeta.setCustomModelData(Config.CUSTOM_MODEL_DATA);
            }
            if (Config.ENCHANTED_COIN)
            {
                baseCoinMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                baseCoinMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            baseCoin.setItemMeta(baseCoinMeta);
        }

        this.withdrawnCoin = coins.meta(baseCoin.clone()).data(CoinUtil.COINS_TYPE, CoinUtil.TYPE_WITHDRAWN);
        MetaBuilder droppedCoinItem = coins.meta(baseCoin.clone()).name(Config.DROPPED_COIN_NAME).data(CoinUtil.COINS_TYPE, CoinUtil.TYPE_DROPPED);

        if (Config.DROP_EACH_COIN)
        {
            droppedCoinItem.data(CoinUtil.COINS_WORTH, 1D);
        }

        this.droppedCoin = droppedCoinItem;
        this.otherCoin = coins.meta(baseCoin.clone()).name(Config.DROPPED_COIN_NAME).data(CoinUtil.COINS_TYPE, CoinUtil.TYPE_OTHER);
    }

    public MetaBuilder dropped ()
    {
        return this.droppedCoin.clone();
    }

    public MetaBuilder withdrawn ()
    {
        return this.withdrawnCoin.clone();
    }

    public MetaBuilder other ()
    {
        return this.otherCoin.clone();
    }
}
