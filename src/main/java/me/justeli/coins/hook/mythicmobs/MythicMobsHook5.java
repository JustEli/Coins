package me.justeli.coins.hook.mythicmobs;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.core.drops.droppables.ItemDrop;
import me.justeli.coins.Coins;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

public final class MythicMobsHook5
    implements MMHook
{
    private final Coins coins;

    public MythicMobsHook5 (Coins coins)
    {
        this.coins = coins;
    }

    private final static BukkitAPIHelper BUKKIT_API_HELPER = new BukkitAPIHelper();

    @Override
    public boolean isMythicMob (Entity entity)
    {
        return BUKKIT_API_HELPER.isMythicMob(entity);
    }

    @EventHandler
    public void onMythicDropLoad (MythicDropLoadEvent event)
    {
        if (event.getDropName().equalsIgnoreCase("coins"))
        {
            String line = event.getConfig().getLine();
            MythicLineConfig config = event.getConfig();
            BukkitItemStack coin = new BukkitItemStack(this.coins.getCreateCoin().dropped());

            ItemDrop drop = new ItemDrop(line, config, coin);
            event.register(drop);
        }
    }
}
