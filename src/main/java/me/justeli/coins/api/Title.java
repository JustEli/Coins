package me.justeli.coins.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;


public class Title
        extends JavaPlugin
{

    private static void sendPacket (Player player, Object packet)
    {
        try
        {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        }
        catch (Exception ignored) { }
    }

    private static Class<?> getNMSClass (String name)
    {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try
        {
            return Class.forName("net.minecraft.server." + version + "." + name);
        }
        catch (ClassNotFoundException ignored)
        {
            return null;
        }
    }

    public static void sendSubTitle (Player player, Integer fadeIn, Integer stay, Integer fadeOut, String subtitle)
    {
        subtitle = Util.color(subtitle);
        try
        {
            Object e;
            Object chatTitle;
            Object chatSubtitle;
            Constructor<?> subtitleConstructor;
            Object titlePacket;
            Object subtitlePacket;

            String title = "";

            e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
            chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class})
                    .invoke(null, "{\"text\":\"" + title + "\"}");
            subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle")
		            .getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"));
            titlePacket = subtitleConstructor.newInstance(e, chatTitle);
            sendPacket(player, titlePacket);

            e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
            chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class})
                    .invoke(null, "{\"text\":\"" + subtitle + "\"}");
            subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle")
                    .getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
            sendPacket(player, subtitlePacket);

        }
        catch (Exception var11)
        {
            player.sendMessage(subtitle);
        }
    }
}