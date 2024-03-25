package me.justeli.coins.hook.mythicmobs;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.core.drops.droppables.VanillaItemDrop;
import me.justeli.coins.Coins;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

// works for MythicMobs 5.6 and up
public final class MythicMobsHook
    implements MMHook
{
    private final Coins coins;

    public MythicMobsHook (Coins coins)
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

            VanillaItemDrop drop = new VanillaItemDrop(line, config, coin);
            event.register(drop);
        }
    }
}
