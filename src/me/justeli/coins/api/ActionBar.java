package me.justeli.coins.api;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class ActionBar
{
    private String text;

    public ActionBar(String text) {
        this.text = ChatColor.translateAlternateColorCodes('&', text);
    }

    @SuppressWarnings("unchecked")
    public void send (Player player)
    {
        String v = Bukkit.getVersion();
        if (v.contains("1.8") || v.contains("1.9") || v.contains("1.10") || v.contains("1.11"))
        {
            JSONObject json = new JSONObject();
            json.put("text", this.text);
            try
            {
                Object handle = player.getClass().getMethod("getHandle").invoke(player),
                        connection = handle.getClass().getField("playerConnection").get(handle),
                        component = ServerPackage.MINECRAFT.getClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class).invoke(null, json.toString()),
                        packet = ServerPackage.MINECRAFT.getClass("PacketPlayOutChat").getConstructor(ServerPackage.MINECRAFT.getClass("IChatBaseComponent"), byte.class).newInstance(component, (byte) 2);
                connection.getClass().getMethod("sendPacket", ServerPackage.MINECRAFT.getClass("Packet")).invoke(connection, packet);
            } catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }
        else if (v.contains("1.7"))
        {
            player.sendMessage(this.text);
        }
        else
        {
            Bars.sendAction(player, this.text);
        }
    }

}