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
        dropEachCoin,
        preventSplits,
        newerServer,
        takePercentage,
        dropOnDeath,
        onlyExperienceBlocks,
        disableHoppers,
        dropWithAnyDeath
    }

    public enum STRING
    {
        nameOfCoin,
        coinItem,
        pickupMessage,
        deathMessage,
        soundName,
        mobMultiplier,
        currencySymbol,
        multiSuffix,
        skullTexture,
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
        minePercentage,
        pickupPitch,
        pickupVolume,
        limitForLocation,
        percentagePlayerHit,
    }

    public enum ARRAY
    {
        disabledWorlds,
    }
}
