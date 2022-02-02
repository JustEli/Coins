package me.justeli.coins.item;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import me.justeli.coins.util.Util;
import org.bukkit.inventory.ItemStack;

import java.util.SplittableRandom;

/** by Eli on January 30, 2022 **/
public final class CreateCoin
{
    private final Coins coins;

    public CreateCoin (Coins coins)
    {
        this.coins = coins;
    }

    private static final SplittableRandom SPLITTABLE_RANDOM = new SplittableRandom();

    public ItemStack withdrawn (double worth)
    {
        String name = Util.formatAmountAndCurrency(worth == 1
                ? Config.WITHDRAWN_COIN_NAME_SINGULAR
                : Config.WITHDRAWN_COIN_NAME_PLURAL, worth);

        return this.coins.meta(this.coins.getBaseCoin().withdrawn()).data(CoinUtil.COINS_WORTH, worth).name(name).build();
    }

    public ItemStack dropped ()
    {
        ItemStack coin = this.coins.getBaseCoin().dropped();

        if (Config.DROP_EACH_COIN || !Config.STACK_COINS) // or maybe only just DROP_EACH_COIN
        {
            return this.coins.meta(coin).data(CoinUtil.COINS_RANDOM, SPLITTABLE_RANDOM.nextInt()).build();
        }

        return coin;
    }

    public MetaBuilder other ()
    {
        return this.coins.getBaseCoin().other();
    }
}
