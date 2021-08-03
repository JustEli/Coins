package me.justeli.coins.config.api;

import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import org.bukkit.configuration.file.FileConfiguration;

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
    public static void parse ()
    {
        Coins.plugin().saveDefaultConfig();
        Coins.plugin().reloadConfig();

        FileConfiguration config = Coins.plugin().getConfig();

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
                    Object defaultValue = field.get(Config.class);
                    Config.error(String.format(
                            "Config file is missing key called '%s'. Using its default value now (%s). Consider to add this to the config:\n\n%s: %s\n",
                            configKey, defaultValue, configKey.replace(".", ":\n  "), defaultValue
                    ));
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
                    configValue = config.getObject(configKey, configClass);
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
