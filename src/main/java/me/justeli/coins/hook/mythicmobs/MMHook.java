package me.justeli.coins.hook.mythicmobs;

import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

/* Eli @ April 02, 2022 (creation) */
public interface MMHook
    extends Listener
{
    boolean isMythicMob (Entity entity);
}
