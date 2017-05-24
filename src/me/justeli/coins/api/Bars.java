package me.justeli.coins.api;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * Created by Eli on 5/21/2017.
 * spigotPlugins: me.justeli.coins.api
 */
class Bars
{
    static void sendAction (Player p, String message)
    {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
