package me.justeli.coins.hook.mythicmobs;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitItemStack;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.drops.droppables.ItemDrop;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import me.justeli.coins.Coins;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

public final class MythicMobsHook4
    implements MMHook
{
    private final Coins coins;

    public MythicMobsHook4 (Coins coins)
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
