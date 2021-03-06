package me.justeli.coins.config;

import me.justeli.coins.Coins;
import me.justeli.coins.util.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by Eli on 4/24/2017.
 * Spigot Plugins: me.justeli.coins.settings
 */

public enum Message
{
    LOADED_SETTINGS ("&3&oCurrently loaded settings of the Coins configuration."),
    NO_PERMISSION ("&4You do not have access to that command."),
    RELOAD_SUCCESS ("&eConfig of &6Coins &ehas been reloaded in &a{0}ms&e."),
    MINOR_ISSUES ("&c&oThere were some minor errors while reloading, check console."),
    CHECK_SETTINGS ("&e&oYou can check the loaded settings with &f&o/coins settings&e&o."),
    WITHDRAW_COINS ("&e&oYou withdrawn &f&o{$}{0} &e&oand received &f&o{0} coins&e&o for it."),
    NOT_THAT_MUCH ("&cYou are not allowed to withdraw that much."),
    COINS_DISABLED ("&cCoins are disabled in this world."),
    SPAWNED_COINS ("&9Spawned {0} coins in radius {1} around {2}."),
    REMOVED_COINS ("&9Removed a total of {0} coins."),
    INVENTORY_FULL ("&cYou cannot withdraw when your inventory is full!"),

    PLAYER_NOT_FOUND ("&4That player could not be found."),
    COORDS_NOT_FOUND ("&4Those coords or the world couldn't be found."),

    INVALID_AMOUNT ("&cThat is an invalid amount."),
    INVALID_NUMBER ("&cThat is an invalid number."),
    INVALID_RADIUS ("&cThat is an invalid radius."),

    COINS_HELP ("&4:: Help for Coins"),
    DROP_USAGE ("&c/coins drop <player|x,y,z[,world]> <amount> [radius]"),
    REMOVE_USAGE ("&c/coins remove [radius|all] &7- remove coins in a radius"),
    SETTINGS_USAGE ("&c/coins settings &7- list the currently loaded settings"),
    RELOAD_USAGE ("&c/coins reload &7- reload the settings from config.yml"),
    VERSION_CHECK ("&c/coins version &7- check if there's a new release"),
    WITHDRAW_USAGE ("&c/withdraw <worth> [amount] &7- withdraw money into coins")
    ;

    private final String def;

    Message (String def)
    {
        this.def = def;
    }

    private static final HashMap<Message, String> MESSAGES = new HashMap<>();

    @Override
    public String toString ()
    {
        return MESSAGES.get(this).replace("{$}", Config.currencySymbol);
    }

    public static void init (String language)
    {
        JSONObject json = getJson (language);
        if (json == null)
        {
            Coins.console(Level.SEVERE, "Could not find the language file '" +  language + ".json' that was configured.");
        }

        for (Message message : Message.values())
        {
            try
            {
                Object name = json.get(message.name());
                MESSAGES.put(message, Util.color(name.toString()));
            }
            catch (Exception exception)
            {
                Coins.console(Level.WARNING, "The language file you are using is missing a message called '" + message.name()
                        + "', using the default (English) value for it now.");
                MESSAGES.put(message, Util.color(message.def));
            }
        }
    }

    private static JSONObject getJson (String language)
    {
        try
        {
            return (JSONObject) new JSONParser().parse(
                    new InputStreamReader(
                            new FileInputStream(
                                    Coins.plugin().getDataFolder() + File.separator + "language" + File.separator + language + ".json"
                            ),
                            StandardCharsets.UTF_8
                    )
            );
        }
        catch (Exception exception)
        {
            return null;
        }
    }
}
