package me.justeli.coins.settings;

import me.justeli.coins.main.Coins;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Eli on 12/14/2016.
 *
 */

public class Settings
{
    public final static HashMap<Config.BOOLEAN, Boolean> hB = new HashMap<>();
    public final static HashMap<Config.STRING, String> hS = new HashMap<>();
    public final static HashMap<Config.DOUBLE, Double> hD = new HashMap<>();
    public final static HashMap<Config.ARRAY, List<String>> hA = new HashMap<>();

    final static HashMap<Messages, String> language = new HashMap<>();
    public final static HashMap<EntityType, Integer> multiplier = new HashMap<>();

    private static FileConfiguration getFile ()
    {
        File config = new File( Coins.getInstance().getDataFolder() + File.separator + "config.yml" );
        if (!config.exists())
            Coins.getInstance().saveDefaultConfig();
        return YamlConfiguration.loadConfiguration( config );
    }

    public static boolean enums ()
    {
        FileConfiguration file = getFile();
        int errors = 0;

        try
        {
            for (Config.BOOLEAN s : Config.BOOLEAN.values())
                hB.put(s, file.getBoolean( s.name() ) );

            for (Config.STRING s : Config.STRING.values())
            {
                if (file.getString( s.name() ) != null)
                    hS.put(s, file.getString( s.name() ) );
                else if (s.equals(Config.STRING.currencySymbol))
                {
                    errorMessage(Msg.OUTDATED_CONFIG, new String[] {"currencySymbol: $"});
                    hS.put(s, "$" );
                    errors ++;
                }
                else
                    { errorMessage (Msg.OUTDATED_CONFIG, null); errors++; }
            }

            for (Config.DOUBLE s : Config.DOUBLE.values())
                hD.put(s, file.getDouble( s.name().replace('_', '.') ) );

            for (Config.ARRAY s : Config.ARRAY.values())
                hA.put(s, file.getStringList( s.name() ) );

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
                    errorMessage( Msg.NO_SUCH_ENTITY, new String[] {key.toUpperCase()} );
                    return false;
                }
            }
        }
        catch (NullPointerException e)
        {
            errorMessage (Msg.OUTDATED_CONFIG, null);
            return false;
        }

        boolean langErr = setLanguage();
        if (!langErr) errors++;

        return errors == 0;
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
        StringBuilder message = new StringBuilder( Messages.LOADED_SETTINGS.toString() + "\n&r" );
        for (Config.STRING s : Config.STRING.values())
            if (!s.equals(Config.STRING.mobMultiplier))
                message.append(s.toString()).append(" &7\u00BB &8").append(hS.get(s)).append("\n&r");

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
            if (!s.equals(Config.BOOLEAN.olderServer))
                message.append(s.toString()).append(" &7\u00BB ")
                        .append(hB.get(s).toString().replace("true", "&atrue").replace("false", "&cfalse")).append("\n&r");

        for (Config.DOUBLE s : Config.DOUBLE.values())
            message.append(s.toString().replace("_", " &o")).append(" &7\u00BB &e").append(hD.get(s)).append("\n&r");

        for (Config.ARRAY s : Config.ARRAY.values())
            message.append(s.toString()).append(" &7\u00BB &b").append(hA.get(s)).append("\n&r");

        return message.toString();
    }

    private static boolean setLanguage ()
    {
        File dirLang = new File(Coins.getInstance().getDataFolder() + File.separator + "language" + File.separator);
        if (!dirLang.exists())
        {
            Coins.getInstance().saveResource("language/english.json", false);
            Coins.getInstance().saveResource("language/dutch.json", false);
            Coins.getInstance().saveResource("language/spanish.json", false);
            Coins.getInstance().saveResource("language/swedish.json", false);
            Coins.getInstance().saveResource("language/german.json", false);
        }

        FileConfiguration file = getFile();
        String lang = getLanguage();
        boolean failure = false;

        if (lang == null)
        {
            failure = true;
            lang = "english";
        }

        try
        {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(new InputStreamReader(new FileInputStream(Coins.getInstance().getDataFolder()
                    + File.separator + "language" + File.separator + lang + ".json"), "UTF-8"));
            JSONObject json = (JSONObject) object;
            for (Messages m : Messages.values())
                language.put( m, json.get(m.name()).toString() );
        }
        catch (FileNotFoundException e)
        { errorMessage(Msg.LANG_NOT_FOUND, new String[] {file.getString("language")}); return false; }
        catch (ParseException | IOException e)
        { e.printStackTrace(); return false; }

        return !failure;
    }

    public static String getLanguage ()
    {
        try
        {
            FileConfiguration file = getFile();
            return file.getString("language").toLowerCase();
        }
        catch (NullPointerException e)
        { errorMessage(Msg.OUTDATED_CONFIG, new String[] {"language: english"}); return null; }
    }

    public enum Msg
    {
        OUTDATED_CONFIG,
        LANG_NOT_FOUND,
        NO_SUCH_ENTITY,
        NO_SUCH_SOUND,
        NO_ECONOMY_SUPPORT
    }

    public static void errorMessage (Msg msg, String[] input)
    {
        switch (msg)
        {
            case OUTDATED_CONFIG:
                System.err.print("Your config of Coins is outdated, update the Coins config.yml.");
                System.err.print("You can copy it from here: https://github.com/JustEli/Coins/blob/master/src/config.yml");
                System.err.print("Use /coins reload afterwards. You could also remove the config if you haven't configured it.");
                System.err.print("The config is probably missing (add it): " + Arrays.toString(input));
                break;
            case LANG_NOT_FOUND:
                System.err.print("The language '" + input[0] + "' that you set in your config does not exist.");
                System.err.print("Check all available languages in the folder 'Coins/language'.");
                break;
            case NO_SUCH_ENTITY:
                System.err.print("There is no entity with the name " + input[0] + ", please change the Coins config.");
                System.err.print("Get types from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");
                break;
            case NO_SUCH_SOUND:
                System.err.print( input[0] + ": the sound does not exist. Change it in the Coins config." );
                System.err.print( "Please use a sound from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html" );
                break;
            case NO_ECONOMY_SUPPORT:
                System.err.print( "======= There seems to be no Vault or economy supportive plugin installed." );
                System.err.print( "======= Please install Vault and an economy supportive plugin like Essentials." );
                System.err.print( "======= Coins will be disabled now.." );
        }

    }

}
