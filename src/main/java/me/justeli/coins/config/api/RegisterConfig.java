package me.justeli.coins.config.api;

import me.justeli.coins.Coins;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by Eli on July 09, 2021.
 * Coins: me.justeli.coins.config.api
 */
public class RegisterConfig
{
    private final JavaPlugin javaPlugin;
    private final Class<?> clazz;

    private RegisterConfig (JavaPlugin javaPlugin, Class<?> clazz)
    {
        this.javaPlugin = javaPlugin;
        this.clazz = clazz;
    }

    public static RegisterConfig of (JavaPlugin javaPlugin, Class<?> clazz)
    {
        return new RegisterConfig(javaPlugin, clazz);
    }

    // returns the time it took to load
    public long parse ()
    {
        long start = System.currentTimeMillis();

        FileConfiguration config = javaPlugin.getConfig();
        for (Field field : clazz.getDeclaredFields())
        {
            if (field.isAnnotationPresent(ConfigFile.class))
            {
                ConfigFile set = field.getAnnotation(ConfigFile.class);
                field.setAccessible(true);

                String configKey = set.value();
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
                else if (configClass == Float.class)
                {
                    configValue = (float) config.getDouble(configKey);
                }
                else
                {
                    configValue = config.getObject(configKey, configClass);
                }

                try
                {
                    if (configValue == null) // todo improve
                    {
                        Object defaultValue = field.get(clazz);

                        Coins.console(Level.WARNING, "The config is missing the option '" + configKey + "'. Using default (" + defaultValue
                                + ") now. Consider adding `" + configKey + ": " + defaultValue + "` to the config.");
                        continue;
                    }

                    field.set(clazz, configValue);
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        return System.currentTimeMillis() - start;
    }

    public Map<String, Object> keys ()
    {
        Map<String, Object> values = new HashMap<>();

        for (Field field : clazz.getDeclaredFields())
        {
            if (field.isAnnotationPresent(ConfigFile.class))
            {
                ConfigFile set = field.getAnnotation(ConfigFile.class);
                field.setAccessible(true);

                try
                {
                    values.put(set.value(), field.get(clazz));
                }
                catch (IllegalAccessException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        return values;
    }
}
