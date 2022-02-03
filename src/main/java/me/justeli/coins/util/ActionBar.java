package me.justeli.coins.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public final class ActionBar
{
    private final String text;

    public ActionBar (String text, double amount)
    {
        this.text = Util.color(Util.formatAmountAndCurrency(text, amount));
    }

    public void send (Player player)
    {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.text));
    }
}