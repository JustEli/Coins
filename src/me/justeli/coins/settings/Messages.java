package me.justeli.coins.settings;

/**
 * Created by Eli on 4/24/2017.
 * Spigot Plugins: me.justeli.coins.settings
 */

public enum Messages
{
    LOADED_SETTINGS,
    NO_PERMISSION,
    RELOAD_SUCCESS,
    MINOR_ISSUES,
    CHECK_SETTINGS,
    INVALID_AMOUNT,
    WITHDRAW_COINS,
    NOT_THAT_MUCH,
    WITHDRAW_USAGE,
    INVALID_NUMBER,
    PLAYER_NOT_FOUND,
    COORDS_NOT_FOUND,
    COINS_DISABLED,
    INVALID_RADIUS,
    SPAWNED_COINS,
    DROP_USAGE,
    COINS_HELP,
    REMOVE_USAGE,
    SETTINGS_USAGE,
    RELOAD_USAGE,
    REMOVED_COINS,
    ;

    @Override
    public String toString ()
    {
        return Settings.language.get(this).replace("{$}", Settings.hS.get(Config.STRING.currencySymbol));
    }
}
