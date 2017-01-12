package me.justeli.coins.settings;

import me.justeli.coins.main.Load;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eli on 12/14/2016.
 */

public class Settings {

    public final static HashMap<Config.BOOLEAN, Boolean> hB = new HashMap<>();
    public final static HashMap<Config.STRING, String> hS = new HashMap<>();
    public final static HashMap<Config.DOUBLE, Double> hD = new HashMap<>();
    public final static HashMap<Config.ARRAY, List<String>> hA = new HashMap<>();

    public final static HashMap<EntityType, Integer> multiplier = new HashMap<>();

    public static boolean enums ()
    {
        File config = new File( Load.main.getDataFolder() + File.separator + "config.yml" );
        if (!config.exists())
            Load.main.saveDefaultConfig();
        FileConfiguration file = YamlConfiguration.loadConfiguration( config );

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
            hB.put(s, file.getBoolean( s.name() ) );

        for (Config.STRING s : Config.STRING.values())
            hS.put(s, file.getString( s.name() ) );

        for (Config.DOUBLE s : Config.DOUBLE.values())
            hD.put(s, file.getDouble( s.name().replace('_', '.') ) );

        for (Config.ARRAY s : Config.ARRAY.values())
            hA.put(s, file.getStringList( s.name() ) );

        try
        {
            Set<String> keys = file.getConfigurationSection( Config.STRING.mobMultiplier.name() ).getKeys(false);
            for ( String key :  keys)
            {
                try
                {
                    EntityType type = EntityType.valueOf( key.toUpperCase() );
                    multiplier.put(type, file.getInt( Config.STRING.mobMultiplier.name() + "." + key ));
                }
                catch (IllegalArgumentException e)
                {
                    System.err.print("There is no entity with the name " + key.toUpperCase() + ", change it.");
                    System.err.print("Get types from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");
                    return false;
                }
            }
        }
        catch (NullPointerException e)
        {
            System.err.print("Your config of Coins is outdated, update the Coins config.yml.");
            System.err.print("You can copy it from here: https://github.com/JustEli/Coins/blob/master/src/config.yml");
            System.err.print("Use /coins reload afterwards. You could also remove the config if you haven't configured it.");
            return false;
        }

        return true;
    }

    public static void remove ()
    {
        multiplier.clear();
        hB.clear();
        hS.clear();
        hD.clear();
        hA.clear();
    }

    public static String getSettings ()
    {
        String message = "&3&oCurrently loaded settings of the Coins configuration.\n&r";
        for (Config.STRING s : Config.STRING.values())
            if (!s.equals(Config.STRING.mobMultiplier))
                message += s.toString() + " &7\u00BB &8" + hS.get(s) + "\n&r";

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
            if (!s.equals(Config.BOOLEAN.olderServer))
                message += s.toString() + " &7\u00BB " + hB.get(s).toString().replace("true", "&atrue").replace("false", "&cfalse") + "\n&r";

        for (Config.DOUBLE s : Config.DOUBLE.values())
            message += s.toString().replace("_", " &o") + " &7\u00BB &e" + hD.get(s) + "\n&r";

        for (Config.ARRAY s : Config.ARRAY.values())
            message += s.toString() + " &7\u00BB &b" + hA.get(s) + "\n&r";

        return message;
    }

}
