package me.justeli.coins.api;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class ActionBar {
	
    @SuppressWarnings("unchecked")
    private static JSONObject convert(String text) {
        JSONObject json = new JSONObject();
        json.put("text", ChatColor.translateAlternateColorCodes('&', text));
        return json;
    }

    private JSONObject json;

    public ActionBar(String text) {
        this.json = convert(text);
    }

    public void send (Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player),
                    connection = handle.getClass().getField("playerConnection").get(handle),
                    component = ServerPackage.MINECRAFT.getClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class).invoke(null, json.toString()),
                    packet = ServerPackage.MINECRAFT.getClass("PacketPlayOutChat").getConstructor(ServerPackage.MINECRAFT.getClass("IChatBaseComponent"), byte.class).newInstance(component, (byte) 2);
            connection.getClass().getMethod("sendPacket", ServerPackage.MINECRAFT.getClass("Packet")).invoke(connection, packet);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}