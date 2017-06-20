package me.justeli.coins.settings;

/**
 * Created by Eli on 12/14/2016.
 *
 */

public enum Config
{
    ;
    public enum BOOLEAN
    {
        stackCoins,
        spawnerDrop,
        passiveDrop,
        pickupSound,
        loseOnDeath,
        olderServer,
        playerDrop,
        preventAlts,
        enableWithdraw,
    }

    public enum STRING
    {
        nameOfCoin,
        pickupMessage,
        deathMessage,
        soundName,
        mobMultiplier,
        currencySymbol,
    }

    public enum DOUBLE
    {
        dropChance,
        maxWithdrawAmount,
        moneyAmount_from,
        moneyAmount_to,
        moneyTaken_from,
        moneyTaken_to,
        moneyDecimals,
    }

    public enum ARRAY
    {
        disabledWorlds,
    }



}
