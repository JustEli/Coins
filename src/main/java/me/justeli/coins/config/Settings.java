package me.justeli.coins.config;

import me.justeli.coins.Coins;
import me.justeli.coins.config.api.RegisterConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Eli on 12/14/2016.
 * Rewritten by Eli on July 9, 2021.
 */

public class Settings
{
    private static RegisterConfig REGISTER_CONFIG;

    public static void init ()
    {
        REGISTER_CONFIG = RegisterConfig.of(Coins.plugin(), Config.class);

        for (String language : new String[]{"english", "dutch", "spanish", "german", "french", "swedish", "chinese", "hungarian"})
        {
            if (!new File(Coins.plugin().getDataFolder() + File.separator + "language" + File.separator + language + ".json").exists())
            {
                Coins.plugin().saveResource("language/" + language + ".json", false);
                Coins.console(Level.INFO, "Added the language file '" + language + "' to Coins, which can be used in the config now.");
            }
        }

        reload();
    }

    public static void reload ()
    {
        Coins.plugin().saveDefaultConfig();
        Coins.plugin().reloadConfig();

        REGISTER_CONFIG.parse();
        Message.init(Config.language);
    }

    public static List<String> get ()
    {
        List<String> items = new ArrayList<>();

        for (Map.Entry<String, Object> part : REGISTER_CONFIG.keys().entrySet())
        {
            items.add(part.getKey() + "\u00BB " + part.getValue());
        }

        return items;
    }
}
