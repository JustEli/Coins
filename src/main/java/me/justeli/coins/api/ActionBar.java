package me.justeli.coins.api;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class ActionBar
{
    private final String text;

    public ActionBar (String text)
    {
        this.text = Util.color(text);
    }

    @SuppressWarnings ("unchecked")
    public void send (Player player)
    {
        switch (PaperLib.getMinecraftVersion())
        {
            case 7:
            {
                player.sendMessage(this.text);
                return;
            }
            case 8:
            case 9:
            case 10:
            case 11:
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
                return;
            }
            default:
            {
                try
                {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(this.text));
                }
                catch (NoClassDefFoundError e)
                {
                    Coins.console(Coins.LogType.ERROR, "You seem to be using Bukkit, but the plugin Coins requires at least Spigot! " +
                            "This prevents the plugin from showing the amount of money players pick up. Please use Spigot. Moving from Bukkit to " +
                            "Spigot will NOT cause any problems with other plugins, since Spigot only adds more features to Bukkit.");
                }
            }
        }
    }
}