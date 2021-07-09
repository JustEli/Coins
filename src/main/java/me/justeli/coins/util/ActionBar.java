package me.justeli.coins.util;

import io.papermc.lib.PaperLib;
import me.justeli.coins.config.Config;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class ActionBar
{
    private final String text;

    private ActionBar (String text)
    {
        this.text = Util.color(text);
    }

    public static ActionBar of (String text)
    {
        return new ActionBar(text.replace("{$}", Config.currencySymbol));
    }

    public void send (Player player)
    {
        if (PaperLib.getMinecraftVersion() >= 10)
        {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.text));
        }
        else
        {
            JSONObject json = new JSONObject();
            json.put("text", this.text);
            try
            {
                Object handle = player.getClass().getMethod("getHandle").invoke(player), connection = handle.getClass()
                        .getField("playerConnection").get(handle), component = ServerPackage.MINECRAFT
                        .getClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class)
                        .invoke(null, json.toString()), packet = ServerPackage.MINECRAFT.getClass("PacketPlayOutChat")
                        .getConstructor(ServerPackage.MINECRAFT.getClass("IChatBaseComponent"), byte.class)
                        .newInstance(component, (byte) 2);
                connection.getClass().getMethod("sendPacket", ServerPackage.MINECRAFT.getClass("Packet"))
                        .invoke(connection, packet);
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    enum ServerPackage
    {
        MINECRAFT("net.minecraft.server." + getServerVersion());

        private final String path;

        ServerPackage (String path)
        {
            this.path = path;
        }

        @Override
        public String toString ()
        {
            return path;
        }

        public Class<?> getClass (String className)
        throws ClassNotFoundException
        {
            return Class.forName(this.toString() + "." + className);
        }

        public static String getServerVersion ()
        {
            return Bukkit.getServer().getClass().getPackage().getName().substring(23);
        }
    }
}