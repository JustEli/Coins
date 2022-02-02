package me.justeli.coins.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public final class ActionBar
{
    private final String text;

    private ActionBar (String text)
    {
        this.text = Util.color(text);
    }

    public static ActionBar of (String text)
    {
        return new ActionBar(Util.formatCurrency(text));
    }

    public void send (Player player)
    {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.text));
    }
}