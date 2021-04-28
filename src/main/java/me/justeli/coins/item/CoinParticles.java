package me.justeli.coins.item;

import me.justeli.coins.Coins;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public class CoinParticles
{
    private static final Random RANDOM = new Random();

    public static void dropCoins (Location location, int radius, int amount)
    {
        Location l = location.add(0.0, 0.5, 0.0);
        ItemStack coin = new Coin().unique().item();
        ItemMeta meta = coin.getItemMeta();

        for (int i = 0; i < amount; i++)
        {
            later(i, () ->
            {
                meta.setLore(Collections.singletonList(UUID.randomUUID().toString()));
                coin.setItemMeta(meta);
                Item item = l.getWorld().dropItem(l, coin);
                item.setPickupDelay(30);
                item.setVelocity(new Vector((RANDOM.nextDouble() - 0.5) * radius / 10, RANDOM.nextDouble() * radius / 5, (RANDOM.nextDouble() - 0.5) * radius / 10));
            });
        }
    }

    private static void later (int ticks, Runnable runnable)
    {
        new BukkitRunnable()
        {
            @Override
            public void run ()
            {
                runnable.run();
            }
        }.runTaskLater(Coins.getInstance(), ticks);
    }

}
