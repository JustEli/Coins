package me.justeli.coins.config.api;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Eli on July 09, 2021.
 * Coins: me.justeli.coins.config.api
 */
public class RegisterConfig
{
    private static FileConfiguration config ()
    {
        File config = new File(Coins.plugin().getDataFolder() + File.separator + "config.yml");
        if (!config.exists())
        {
            Coins.plugin().saveDefaultConfig();
        }
        return YamlConfiguration.loadConfiguration(config);
    }

    public static void parse ()
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
                if (!config.contains(configKey))
                {
                    if (configEntry.required())
                    {
                        String presuf = field.getType() == String.class? "'" : "";
                        Object defaultValue = presuf + field.get(Config.class) + presuf;

                        Config.error(String.format(
                                "\nConfig file is missing key called '%s'. Using its default value now (%s)."
                                        + (configEntry.motivation().isEmpty()? "" : " " + configEntry.motivation())
                                        + " Consider to add this to the config:\n----------------------------------------\n%s: %s" +
                                        "\n----------------------------------------",
                                configKey, defaultValue, configKey.replace(".", ":\n  "), defaultValue
                        ));
                    }
                    continue;
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
                    configValue = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (Integer) e.getValue()));
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
                    Config.error(String.format(
                            "Config file has wrong value for key called '%s'. Using its default value now (%s).", configKey, defaultValue
                    ));
                }
                catch (IllegalAccessException illegalException)
                {
                    illegalException.printStackTrace();
                }
            }
        }

        Config.parse();
    }

    public static Map<String, Object> keys ()
    {
        Map<String, Object> values = new HashMap<>();

        for (Field field : Config.class.getDeclaredFields())
        {
            if (field.isAnnotationPresent(ConfigEntry.class))
            {
                ConfigEntry set = field.getAnnotation(ConfigEntry.class);
                field.setAccessible(true);

                try
                {
                    values.put(set.value(), field.get(Config.class));
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        return values;
    }
}
