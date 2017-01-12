package me.justeli.coins.settings;

/**
 * Created by Eli on 12/14/2016.
 */

public enum Config {

    ;

    public enum BOOLEAN {

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

    public enum STRING {

        nameOfCoin,
        pickupMessage,
        soundName,
        mobMultiplier,

    }

    public enum DOUBLE {

        dropChance,
        maxWithdrawAmount,

        moneyAmount_from,
        moneyAmount_to,

        moneyTaken_from,
        moneyTaken_to,

    }

    public enum ARRAY {

        disabledWorlds,

    }



}
