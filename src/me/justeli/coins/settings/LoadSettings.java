package me.justeli.coins.settings;

import me.justeli.coins.main.Load;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Eli on 12/14/2016.
 */

public class LoadSettings {

    public final static HashMap<Setting._Boolean, Boolean> hB = new HashMap<>();
    public final static HashMap<Setting._String, String> hS = new HashMap<>();
    public final static HashMap<Setting._Double, Double> hD = new HashMap<>();
    public final static HashMap<Setting._Array, List<String>> hA = new HashMap<>();

    public static void enums ()
    {
        File config = new File( Load.main.getDataFolder() + File.separator + "config.yml" );
        if (!config.exists())
            Load.main.saveDefaultConfig();
        FileConfiguration file = YamlConfiguration.loadConfiguration( config );

        for (Setting._Boolean s : Setting._Boolean.values())
            hB.put(s, file.getBoolean( s.name() ) );

        for (Setting._String s : Setting._String.values())
            hS.put(s, file.getString( s.name() ) );

        for (Setting._Double s : Setting._Double.values())
            hD.put(s, file.getDouble( s.name().replace('_', '.') ) );

        for (Setting._Array s : Setting._Array.values())
            hA.put(s, file.getStringList( s.name() ) );

    }

    public static void remove ()
    {
        for (Setting._Boolean s : Setting._Boolean.values()) hB.remove(s);
        for (Setting._String s : Setting._String.values()) hS.remove(s);
        for (Setting._Double s : Setting._Double.values()) hD.remove(s);
        for (Setting._Array s : Setting._Array.values()) hA.remove(s);
    }

    public static String getSettings ()
    {
        String message = "&3&oCurrently loaded settings of the Coins configuration.\n&r";
        for (Setting._String s : Setting._String.values())
            message += s.toString() + " &7\u00BB &8" + hS.get(s) + "\n&r";

        for (Setting._Boolean s : Setting._Boolean.values())
            message += s.toString() + " &7\u00BB " + hB.get(s).toString().replace("true", "&atrue").replace("false", "&cfalse") + "\n&r";

        for (Setting._Double s : Setting._Double.values())
            message += s.toString().replace("_", " &o") + " &7\u00BB &e" + hD.get(s) + "\n&r";

        for (Setting._Array s : Setting._Array.values())
            message += s.toString() + " &7\u00BB &b" + hA.get(s) + "\n&r";

        return message;
    }

}
