package me.justeli.coins.api;

import io.papermc.lib.PaperLib;
import org.bukkit.entity.*;


/**
 * Created by Eli on January 18, 2021.
 * Coins: me.justeli.coins.api
 */
public class IsEntity
{
    public static boolean hostile (Entity entity)
    {
        return entity instanceof Monster
                || entity instanceof Flying
                || entity instanceof Slime
                || (entity instanceof Golem && !(entity instanceof Snowman))
                || entity instanceof Wolf
                || (PaperLib.getMinecraftVersion() >= 14? entity instanceof Boss : entity instanceof EnderDragon);
    }

    public static boolean player (Entity entity)
    {
        return entity instanceof Player;
    }

    public static boolean passive (Entity entity)
    {
        return !hostile(entity)
                && !player(entity)
                && entity instanceof LivingEntity;
    }
}
