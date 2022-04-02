package me.justeli.coins.hook.mythicmobs;

import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

/** by Eli on April 02, 2022 **/
public interface MMHook
        extends Listener
{
    boolean isMythicMob (Entity entity);
}
