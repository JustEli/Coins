package me.justeli.coins.settings;

/**
 * Created by Eli on 12/14/2016.
 */

public enum Setting {

    ;

    public enum _Boolean {

        stackCoins,
        spawnerDrop,
        passiveDrop,
        pickupSound,
        loseOnDeath,
        olderServer,
        ;

    }

    public enum _String {

        nameOfCoin,
        pickupMessage,
        soundName,
        ;

    }

    public enum _Double {

        dropChance,

        moneyAmount_from,
        moneyAmount_to,

        moneyTaken_from,
        moneyTaken_to,
        ;

    }

    public enum _Array {

        disabledWorlds,
        ;

    }



}
