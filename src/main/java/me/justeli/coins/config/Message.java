package me.justeli.coins.config;

import java.util.HashMap;

/** Created by Eli on 4/24/2017. */
public enum Message
{
    NO_PERMISSION,
    RELOAD_SUCCESS,
    MINOR_ISSUES,
    CHECK_SETTINGS,
    WITHDRAW_COINS,
    NOT_THAT_MUCH,
    COINS_DISABLED,
    SPAWNED_COINS,
    REMOVED_COINS,
    INVENTORY_FULL,

    PLAYER_NOT_FOUND,
    COORDS_NOT_FOUND,

    INVALID_AMOUNT,
    INVALID_NUMBER,
    INVALID_RADIUS,

    DROP_USAGE,
    REMOVE_USAGE ,
    SETTINGS_USAGE,
    RELOAD_USAGE,
    VERSION_CHECK,
    WITHDRAW_USAGE,
    TOGGLE_USAGE,

    DISABLED_REASONS,
    CURRENTLY_INSTALLED,
    LATEST_RELEASE,
    UP_TO_DATE,
    LATEST_RETRIEVE_FAIL,

    ENABLED,
    DISABLED,

    GLOBALLY_DISABLED_INFORM,
    DISABLED_DESCRIPTION,
    WITHDRAWING_DISABLED,
    GLOBALLY_DISABLED,
    OUTDATED,
    ;

    static final HashMap<Message, String> MESSAGES = new HashMap<>();

    @Override
    public String toString ()
    {
        return MESSAGES.computeIfAbsent(this, empty -> "Error while retrieving message");
    }

    public String replace (CharSequence... replacements)
    {
        String message = toString();
        for (int i = 0; i < replacements.length; i++)
        {
            message = message.replace("{" + i + "}", replacements[i].toString());
        }
        return message;
    }
}
