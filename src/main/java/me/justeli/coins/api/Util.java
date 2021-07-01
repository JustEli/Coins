package me.justeli.coins.api;

import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eli on April 28, 2021.
 * Coins: me.justeli.coins.api
 */
public class Util
{
    private static final Pattern rgbColor = Pattern.compile("(?<!\\\\)(&#[a-fA-F0-9]{6})");

    public static String color (String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', parseRGB(msg));
    }

    private static String parseRGB (String msg)
    {
        if (PaperLib.getMinecraftVersion() >= 16)
        {
            Matcher matcher = rgbColor.matcher(msg);
            while (matcher.find())
            {
                String color = msg.substring(matcher.start(), matcher.end());
                String hex = color.replace("&", "").toUpperCase();

                msg = msg.replace(color, ChatColor.of(hex).toString());
                matcher = rgbColor.matcher(msg);
            }
        }

        return msg;
    }
}
