package me.justeli.coins.item;

import me.justeli.coins.Coins;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

/* Eli @ January 30, 2022 (creation) */
public final class CoinUtil
{
    private final Coins coins;

    public CoinUtil (Coins coins)
    {
        this.coins = coins;
    }

    // three types of tags:
    //  - coins-type:       Integer  TYPE_DROPPED, TYPE_WITHDRAWN, TYPE_OTHER
    //  - coins-worth:      Double   coin worth if not random amount
    //  - coins-random:     Integer  a random number prevents item to stack
    //  - coins-increment:  Double   looting or fortune coin value increment

    public static final String COINS_TYPE = "coins-type"; // 1 = dropped, 2 = withdrawn, 3 = other
    public static final int TYPE_DROPPED = 1;
    public static final int TYPE_WITHDRAWN = 2;
    public static final int TYPE_OTHER = 3;

    public static final String COINS_WORTH = "coins-worth";
    public static final String COINS_RANDOM = "coins-random";
    public static final String COINS_INCREMENT = "coins-increment";

    public boolean isCoin (ItemStack item)
    {
        if (item == null)
            return false;

        return this.coins.meta(item).data(COINS_TYPE, PersistentDataType.INTEGER).isPresent();
    }

    public boolean isDroppedCoin (ItemStack item)
    {
        if (item == null)
            return false;

        return this.coins.meta(item).data(COINS_TYPE, PersistentDataType.INTEGER).orElse(0) == TYPE_DROPPED;
    }

    public boolean isWithdrawnCoin (ItemStack item)
    {
        if (item == null)
            return false;

        return this.coins.meta(item).data(COINS_TYPE, PersistentDataType.INTEGER).orElse(0) == TYPE_WITHDRAWN;
    }

    public double getValue (ItemStack item)
    {
        if (item == null)
            return 0;

        Optional<Double> worth = this.coins.meta(item).data(COINS_WORTH, PersistentDataType.DOUBLE);
        return worth.map(value -> value * item.getAmount()).orElse(0.0);

    }

    public double getIncrement (ItemStack item)
    {
        return this.coins.meta(item).data(COINS_INCREMENT, PersistentDataType.DOUBLE).orElse(1D);
    }
}
