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
        playerDrop,
        preventAlts,
        enableWithdraw,
        dropEachCoin,
        preventSplits,
        takePercentage,
        dropOnDeath,
        onlyExperienceBlocks,
        disableHoppers,
        dropWithAnyDeath,
        enchantedCoin,
    }

    public enum STRING
    {
        nameOfCoin,
        coinItem,
        pickupMessage,
        deathMessage,
        soundName,
        mobMultiplier,
        blockMultiplier,
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
        soundPitch,
        soundVolume,
        limitForLocation,
        percentagePlayerHit,
        customModelData,
    }

    public enum ARRAY
    {
        disabledWorlds,
    }
}
