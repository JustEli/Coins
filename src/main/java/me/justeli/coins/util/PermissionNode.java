package me.justeli.coins.util;

/* Eli @ February 10, 2022 (creation) */
public final class PermissionNode
{
    public static final String COMMAND_RELOAD = "coins.command.reload";
    public static final String COMMAND_SETTINGS = "coins.command.settings";
    public static final String COMMAND_DROP = "coins.command.drop";
    public static final String COMMAND_REMOVE = "coins.command.remove";
    public static final String COMMAND_LANGUAGE = "coins.command.language";
    public static final String COMMAND_VERSION = "coins.command.version";
    public static final String COMMAND_TOGGLE = "coins.command.toggle";
    public static final String WITHDRAW = "coins.withdraw";
    public static final String DISABLE = "coins.disable";
    public static final String SPAWNER = "coins.spawner";

    public static final String MULTIPLIER_PREFIX = "coins.multiplier.";

    public static double multiplierFromPermission (String fromPermission)
    {
        if (!fromPermission.startsWith(MULTIPLIER_PREFIX))
            return 1D;

        String number = fromPermission.substring(MULTIPLIER_PREFIX.length());
        return Util.parseDouble(number).orElse(1D);
    }
}
