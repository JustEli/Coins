package me.justeli.coins.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Complete
{

    public static Player onlinePlayer (String incomplete)
    {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
            if (player.getName().toLowerCase().startsWith(incomplete.toLowerCase()))
                return player;
            else if (player.getDisplayName().toLowerCase().contains(incomplete.toLowerCase()))
                return player;
            else if (player.getName().toLowerCase().contains(incomplete.toLowerCase()))
                return player;

        return null;
    }

}
