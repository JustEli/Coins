package me.justeli.coins.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public final class SubTitle
{
    private final String subtitle;
    private int fadeIn = 10;
    private int stay = 100;
    private int fadeOut = 20;

    private SubTitle (String subtitle)
    {
        this.subtitle = Util.color(subtitle);
    }

    public static SubTitle of (String text)
    {
        return new SubTitle(Util.formatCurrency(text));
    }

    public SubTitle in (int ticks)
    {
        this.fadeIn = ticks;
        return this;
    }

    public SubTitle out (int ticks)
    {
        this.fadeOut = ticks;
        return this;
    }

    public SubTitle stay (int ticks)
    {
        this.stay = ticks;
        return this;
    }

    public void send (Player player)
    {
        player.sendTitle(ChatColor.RESET.toString(), subtitle, fadeIn, stay, fadeOut);
    }
}