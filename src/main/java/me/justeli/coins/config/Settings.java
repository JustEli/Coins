package me.justeli.coins.config;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import me.justeli.coins.Coins;
import me.justeli.coins.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

/** Created by Eli on 12/14/2016. Rewritten by Eli on July 9, 2021. */
public final class Settings
{
    private final Coins coins;

    public Settings (Coins coins)
    {
        this.coins = coins;
    }

    public void reloadLanguage ()
    {
        for (String language : new String[]{"english", "dutch", "spanish", "german", "french", "swedish", "chinese", "hungarian"})
        {
            if (!new File(this.coins.getDataFolder() + File.separator + "language" + File.separator + language + ".json").exists())
            {
                this.coins.saveResource("language/" + language + ".json", false);
                this.coins.console(Level.INFO, "Added the language '" + language + "' to Coins, which can now be used in the config.");
            }
        }

        initializeMessages(Config.LANGUAGE);
    }

    private FileConfiguration config ()
    {
        File config = new File(this.coins.getDataFolder() + File.separator + "config.yml");
        if (!config.exists())
        {
            this.coins.saveDefaultConfig();
        }
        return YamlConfiguration.loadConfiguration(config);
    }

    public static boolean USING_LEGACY_KEYS; // from before version 1.12
    private static final Converter<String, String> LEGACY_CONVERTER = CaseFormat.LOWER_HYPHEN.converterTo(CaseFormat.LOWER_CAMEL);

    public void parseConfig ()
    {
        FileConfiguration config = config();

        for (Field field : Config.class.getDeclaredFields())
        {
            if (!field.isAnnotationPresent(ConfigEntry.class))
                continue;

            ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
            field.setAccessible(true);
            String configKey = configEntry.value();

            try
            {
                boolean legacyChecked = false;
                if (!config.contains(configKey))
                {
                    configKey = LEGACY_CONVERTER.convert(configKey);
                    legacyChecked = true;
                }

                if (!config.contains(configKey))
                {
                    if (configEntry.required())
                    {
                        String prefixSuffix = field.getType() == String.class? "'" : "";
                        Object defaultValue = prefixSuffix + field.get(Config.class) + prefixSuffix;

                        error(String.format(
                                "\nConfig file is missing key called '%s'. Using its default value now (%s)."
                                        + (configEntry.motivation().isEmpty()? "" : " " + configEntry.motivation())
                                        + " Consider to add this to the config:\n----------------------------------------\n%s: %s" +
                                        "\n----------------------------------------",
                                configEntry.value(), defaultValue, configEntry.value().replace(".", ":\n  "), defaultValue
                        ));
                    }
                    continue;
                }

                if (legacyChecked)
                {
                    USING_LEGACY_KEYS = true;
                }

                Class<?> configClass = field.getType();
                Object configValue;

                if (configClass == Set.class)
                {
                    List<String> stringList = config.getStringList(configKey);
                    configValue = new HashSet<>(stringList);
                }
                else if (configClass == Map.class)
                {
                    Map<String, Object> map = config.getConfigurationSection(configKey).getValues(false);
                    Map<String, Integer> configMap = new HashMap<>();

                    for (Map.Entry<String, Object> mapLoop : map.entrySet())
                    {
                        configMap.put(
                                mapLoop.getKey().toUpperCase(Locale.ROOT).replace(" ", "_"),
                                Util.parseInt(mapLoop.getValue().toString()).orElse(1)
                        );
                    }
                    configValue = configMap;
                }
                // can be improved in java 11
                else if (configClass == Long.class || configClass == Integer.class || configClass == Float.class || configClass == Double.class)
                {
                    Double value = new Double(config.get(configKey, "0").toString());

                    if (configClass == Long.class)
                    {
                        configValue = value.longValue();
                    }
                    else if (configClass == Integer.class)
                    {
                        configValue = value.intValue();
                    }
                    else if (configClass == Float.class)
                    {
                        configValue = value.floatValue();
                    }
                    else
                    {
                        configValue = value;
                    }
                }
                else
                {
                    //configValue = config.getObject(configKey, configClass);
                    configValue = configClass.cast(config.get(configKey));
                }

                field.set(Config.class, configValue);
            }
            catch (Exception exception)
            {
                try
                {
                    Object defaultValue = field.get(Config.class);
                    error(String.format(
                            "Config file has wrong value for key called '%s'. Using its default value now (%s).",
                            configEntry.value(),
                            defaultValue
                    ));
                }
                catch (IllegalAccessException ignored) {}
            }
        }

        parseRemainingOptions();
    }

    private void parseRemainingOptions ()
    {
        Config.DROPPED_COIN_NAME = Util.color(Config.LEGACY_RAW_NAME_OF_COIN == null? Config.RAW_DROPPED_COIN_NAME : Config.LEGACY_RAW_NAME_OF_COIN);

        Config.WITHDRAWN_COIN_NAME_SINGULAR = Util.color(Config.LEGACY_RAW_NAME_OF_COIN == null
                ? Config.RAW_WITHDRAWN_COIN_NAME_SINGULAR
                : Config.LEGACY_PREFIX + Config.LEGACY_RAW_NAME_OF_COIN);

        Config.WITHDRAWN_COIN_NAME_PLURAL = Util.color(Config.LEGACY_MULTI_SUFFIX == null && Config.LEGACY_RAW_NAME_OF_COIN == null
                ? Config.RAW_WITHDRAWN_COIN_NAME_PLURAL
                : Config.LEGACY_PREFIX + Config.LEGACY_RAW_NAME_OF_COIN + Config.LEGACY_MULTI_SUFFIX);

        Config.LEGACY_WITHDRAWN_COIN_ENDING = Config.LEGACY_MULTI_SUFFIX == null && Config.LEGACY_RAW_NAME_OF_COIN == null
                ? null
                : Util.color(Config.LEGACY_RAW_NAME_OF_COIN + Config.LEGACY_MULTI_SUFFIX);

        Config.COIN_ITEM = coinItem();
        Config.SOUND_NAME = soundName();

        if (Config.DETECT_LEGACY_COINS)
        {
            Config.ALLOW_NAME_CHANGE = false;
        }
    }

    private Material coinItem ()
    {
        String material = Config.RAW_COIN_ITEM
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT)
                .replace("COIN", "SUNFLOWER");

        Material coin = Material.matchMaterial(material);

        if (coin == null)
        {
            error("The material '" + Config.RAW_COIN_ITEM + "' in the config at `coin-item` does not exist. Please use a " +
                    "material from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");

            return Material.SUNFLOWER;
        }

        return coin;
    }

    private Sound soundName ()
    {
        try
        {
            return Sound.valueOf(Config.RAW_SOUND_NAME.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException exception)
        {
            error("The sound '" + Config.RAW_SOUND_NAME + "' in the config at `sound-name` does not exist. Please use a " +
                    "sound from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");

            return Sound.ITEM_ARMOR_EQUIP_GOLD;
        }
    }

    private int warnings = 0;

    public void error (String message)
    {
        warnings++;
        this.coins.console(Level.WARNING, "#" + warnings + ": " + message);
    }

    public void resetWarningCount ()
    {
        warnings = 0;
    }

    public int getWarningCount ()
    {
        return warnings;
    }

    private static final Converter<String, String> VAR_CONVERTER = CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.LOWER_HYPHEN);

    public TreeSet<String> getKeys ()
    {
        TreeSet<String> values = new TreeSet<>();

        for (Field field : Config.class.getDeclaredFields())
        {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && !field.isAnnotationPresent(Deprecated.class))
            {
                try
                {
                    values.add(
                            VAR_CONVERTER.convert(field.getName())
                                    + " &8\u00BB&7 "
                                    + Util.formatCurrency(field.get(Config.class).toString())
                    );
                }
                catch (Exception ignored) {}
            }
        }

        return values;
    }

    public void initializeMessages (String language)
    {
        JSONObject json = getLanguageJson(language);
        if (json == null)
        {
            this.coins.console(Level.SEVERE, "Could not find the language file '" +  language + ".json' that was configured.");
        }

        for (Message message : Message.values())
        {
            try
            {
                Object name = json.get(message.name());
                Message.MESSAGES.put(message, Util.color(Util.formatCurrency(name.toString())));
            }
            catch (Exception exception)
            {
                this.coins.settings().error("Language file is missing message called '" + message.name() +
                        "'. Using its default value now (in English).");
                Message.MESSAGES.put(message, Util.color(Util.formatCurrency(message.defaultMessage)));
            }
        }
    }

    private JSONObject getLanguageJson (String language)
    {
        File file = getLanguageFile(language);

        if (file == null)
            return null;

        try (
                FileInputStream fileStream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)
        )
        {
            return (JSONObject) new JSONParser().parse(reader);
        }
        catch (IOException | ParseException exception)
        {
            return null;
        }
    }

    private File getLanguageFile (String language)
    {
        File[] languageFiles = new File(this.coins.getDataFolder().getAbsolutePath() + File.separator + "language").listFiles();
        if (languageFiles == null)
            return null;

        for (File languageFile : languageFiles)
        {
            if (languageFile.getName().equalsIgnoreCase(language + ".json"))
            {
                return languageFile;
            }
        }

        return null;
    }
}
