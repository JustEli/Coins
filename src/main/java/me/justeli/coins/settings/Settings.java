package me.justeli.coins.settings;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.api.Util;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Eli on 12/14/2016.
 */

public class Settings
{
    public final static HashMap<Config.BOOLEAN, Boolean> hB = new HashMap<>();
    public final static HashMap<Config.STRING, String> hS = new HashMap<>();
    public final static HashMap<Config.DOUBLE, Double> hD = new HashMap<>();
    public final static HashMap<Config.ARRAY, List<String>> hA = new HashMap<>();

    final static HashMap<Messages, String> language = new HashMap<>();
    public final static HashMap<EntityType, Integer> mobMultipliers = new HashMap<>();
    public final static HashMap<Material, Integer> blockMultipliers = new HashMap<>();

    private static String COIN_NAME;

    public static String getCoinName ()
    {
        return COIN_NAME;
    }

    private static FileConfiguration getFile ()
    {
        File config = new File(Coins.plugin().getDataFolder() + File.separator + "config.yml");
        if (!config.exists())
            Coins.plugin().saveDefaultConfig();
        return YamlConfiguration.loadConfiguration(config);
    }

    public static boolean load ()
    {
        FileConfiguration file = getFile();
        int errors = 0;

        try
        {
            for (Config.BOOLEAN s : Config.BOOLEAN.values())
                hB.put(s, file.getBoolean(s.name()));

            for (Config.STRING s : Config.STRING.values())
            {
                if (s.equals(Config.STRING.multiSuffix) && file.getString(s.name()) == null)
                {
                    hS.put(s, "s");
                    errorMessage(Msg.OUTDATED_CONFIG, new String[]{"multiSuffix: s", "dropEachCoin: false"});
                }
                else if (s.equals(Config.STRING.coinItem))
                {
                    if (file.getString(s.name()) != null)
                    {
                        try
                        {
                            String material = file.getString(s.name()).toUpperCase().replace(" ", "_");
                            material = material.replace("COIN", PaperLib.getMinecraftVersion() > 12? "SUNFLOWER" : "DOUBLE_PLANT");

                            Material coin = Material.valueOf(material);
                            hS.put(s, coin.name());
                        }
                        catch (IllegalArgumentException | NullPointerException e)
                        {
                            errorMessage(Msg.NO_SUCH_MATERIAL, new String[]{file.getString(s.name())});
                            hS.put(s, "DOUBLE_PLANT");
                            errors++;
                        }
                    }
                    else
                    {
                        errorMessage(Msg.OUTDATED_CONFIG, new String[]{"coinItem: coin"});
                        hS.put(s, "DOUBLE_PLANT");
                        errors++;
                    }
                }
                else if (file.getString(s.name()) != null)
                    hS.put(s, file.getString(s.name()));
                else
                {
                    errorMessage(Msg.OUTDATED_CONFIG, new String[]{s.name()});
                    errors++;
                }
            }

            for (Config.DOUBLE s : Config.DOUBLE.values())
                hD.put(s, file.getDouble(s.name().replace('_', '.')));

            for (Config.ARRAY s : Config.ARRAY.values())
                hA.put(s, file.getStringList(s.name()));

            for (String key : file.getConfigurationSection(Config.STRING.mobMultiplier.name()).getKeys(false))
            {
                try
                {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    mobMultipliers.put(type, file.getInt(Config.STRING.mobMultiplier.name() + "." + key));
                }
                catch (IllegalArgumentException e)
                {
                    errorMessage(Msg.NO_SUCH_ENTITY, new String[]{key.toUpperCase()});
                    return false;
                }
            }
            for (String key : file.getConfigurationSection(Config.STRING.blockMultiplier.name()).getKeys(false))
            {
                try
                {
                    Material type = Material.matchMaterial(key);
                    blockMultipliers.put(type, file.getInt(Config.STRING.blockMultiplier.name() + "." + key));
                }
                catch (IllegalArgumentException e)
                {
                    errorMessage(Msg.NO_SUCH_MATERIAL, new String[]{key.toUpperCase()});
                    return false;
                }
            }
        }
        catch (NullPointerException e)
        {
            errorMessage(Msg.OUTDATED_CONFIG, null);
            return false;
        }

        COIN_NAME = Util.color(Settings.hS.get(Config.STRING.nameOfCoin));

        boolean langErr = setLanguage();
        if (!langErr)
            errors++;

        return errors == 0;
    }

    public static void remove ()
    {
        mobMultipliers.clear();
        blockMultipliers.clear();
        hB.clear();
        hS.clear();
        hD.clear();
        hA.clear();
    }

    public static String getSettings ()
    {
        StringBuilder message = new StringBuilder(Messages.LOADED_SETTINGS.toString() + "\n&r");
        for (Config.STRING s : Config.STRING.values())
            if (!s.equals(Config.STRING.mobMultiplier))
                message.append(s.toString()).append(" &7\u00BB &8").append(hS.get(s)).append("\n&r");

        for (Config.BOOLEAN s : Config.BOOLEAN.values())
            message.append(s.toString()).append(" &7\u00BB ").append(hB.get(s).toString().replace("true", "&atrue").replace("false", "&cfalse")).append("\n&r");

        for (Config.DOUBLE s : Config.DOUBLE.values())
            message.append(s.toString().replace("_", " &o")).append(" &7\u00BB &e").append(hD.get(s)).append("\n&r");

        for (Config.ARRAY s : Config.ARRAY.values())
            message.append(s.toString()).append(" &7\u00BB &b").append(hA.get(s)).append("\n&r");

        return message.toString();
    }

    private static boolean setLanguage ()
    {
        for (String lang : new String[]{"english", "dutch", "spanish", "german", "french", "swedish", "chinese", "hungarian"})
            if (!new File(Coins.plugin().getDataFolder() + File.separator + "language" + File.separator + lang + ".json").exists())
                Coins.plugin().saveResource("language/" + lang + ".json", false);

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
            Object object = parser.parse(new InputStreamReader(new FileInputStream(Coins.plugin()
                    .getDataFolder() + File.separator + "language" + File.separator + lang + ".json"), StandardCharsets.UTF_8));
            JSONObject json = (JSONObject) object;
            for (Messages m : Messages.values())
            {
                try
                {
                    Object name = json.get(m.name());
                    language.put(m, name.toString());
                }
                catch (NullPointerException ex)
                {
                    failure = true;
                    if (m.equals(Messages.INVENTORY_FULL))
                        language.put(m, "&cYou cannot withdraw when your inventory is full!");
                    if (m.equals(Messages.VERSION_CHECK))
                        language.put(m, "&c/coins version &7- check if there's a new release");
                    errorMessage(Msg.NO_TRANSLATION, new String[]{m.name()});
                }
            }
        }
        catch (FileNotFoundException e)
        {
            errorMessage(Msg.LANG_NOT_FOUND, new String[]{file.getString("language")});
            return false;
        }
        catch (ParseException | IOException e)
        {
            e.printStackTrace();
            return false;
        }

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
        {
            errorMessage(Msg.OUTDATED_CONFIG, new String[]{"language: english"});
            return null;
        }
    }

    public enum Msg
    {
        OUTDATED_CONFIG,
        LANG_NOT_FOUND,
        NO_SUCH_ENTITY,
        NO_SUCH_SOUND,
        NO_ECONOMY_SUPPORT,
        NO_SUCH_MATERIAL,
        NO_TRANSLATION,
    }

    public static void errorMessage (Msg msg, String[] input)
    {
        switch (msg)
        {
            case OUTDATED_CONFIG:
                Coins.console(Level.WARNING, "Your config of Coins is outdated, update the Coins config.yml.");
                Coins.console(Level.WARNING, "You can copy it from here: https://github.com/JustEli/Coins/blob/master/src/main/resources/config.yml");
                Coins.console(Level.WARNING, "Use /coins reload afterwards. You could also remove the config if you haven't configured it.");
                if (input != null)
                {
                    Coins.console(Level.WARNING, "This option is probably missing (add it): " + Arrays.toString(input));
                }
                break;
            case LANG_NOT_FOUND:
                Coins.console(Level.WARNING, "The language '" + input[0] + "' that you set in your config does not exist.");
                Coins.console(Level.WARNING, "Check all available languages in the folder 'Coins/language'.");
                break;
            case NO_SUCH_ENTITY:
                Coins.console(Level.WARNING, "There is no entity with the name '" + input[0] + "', please change the Coins config.");
                Coins.console(Level.WARNING, "Get types from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");
                break;
            case NO_SUCH_MATERIAL:
                Coins.console(Level.WARNING, "There is no material with the name '" + input[0] + "', please change the Coins config.");
                Coins.console(Level.WARNING, "Get materials from here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
                break;
            case NO_SUCH_SOUND:
                Coins.console(Level.WARNING, "The sound '" + input[0] + "' does not exist. Change it in the Coins config.");
                Coins.console(Level.WARNING, "Please use a sound from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");
                break;
            case NO_ECONOMY_SUPPORT:
                Coins.console(Level.SEVERE, "There seems to be no Vault or economy supportive plugin installed.");
                Coins.console(Level.SEVERE, "Please install Vault and an economy supportive plugin like Essentials.");
                break;
            case NO_TRANSLATION:
                Coins.console(Level.WARNING, "The translation for '" + input[0] + "' was not found.");
                Coins.console(Level.WARNING, "Please add it to the {language}.json file.");
                Coins.console(Level.WARNING, "Or delete your /language/ folder in /Coins/. RECOMMENDED");
                break;
        }

    }

}
