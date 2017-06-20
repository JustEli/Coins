// ------------------------------- //
// Small and simple API for Coins. //
// ------------------------------- //

// Coins has three parts in the plugin usable for outside developers.
// The classes: Coins, Coin, CoinDropEvent


// You can drop coins in a radius around someone.
import me.justeli.coins.main.Coins;
Coins.particles (Location location, int radius, int amount);

// So you could, for example, do a coin rain on advancement get:
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

@EventHandler
public void advancementRain (PlayerAchievementAwardedEvent e)
{
    Coins.particles (e.getPlayer().getLocation(), 3, 20);
}


// You can create a coin.
import me.justeli.coins.item.Coin;
ItemStack coin = new Coin().item();                 // gets the ItemStack of one coin
ItemStack coin = new Coin().stack(false).item();    // creates coin, but not stackable
ItemStack coin = new Coin().withdraw(100).item();   // makes it a withdrawnable coin



// When 'spawnerDrop' is set to false (default), you can check if a mob is spawned by a spawner.
// Since every mob from a spawner has a hashmap (if 'spawnerDrop' is set to false).

import me.justeli.coins.main.Coins;
boolean mobFromSpawner = Coins.mobFromSpawner (Entity entity);
// Returns true if the Entity is from a spawner, false if not.



// The plugin has its own event, too!
import me.justeli.coins.events.CoinDropEvent;

@EventHandler
public void onCoinDrop (CoinDropEvent e)
{
    if (e.getKiller().hasPermission("no.coins.from.skeletons") && e.getEntityType().equals(EntityType.SKELETON))
    {
        e.setCancelled( true );
        // When the player has the permission 'no.coins.from.skeletons',
        // and the entity killed is a skeleton, it won't drop a coin.
    }

    // The following enables coin drops for mobs from spawners when 'spawnerDrop' is set to false in config.
    // But you can do more advanced stuff, since the plugin can only detect mobs from a spawner if 'spawnerDrop' is set to false.

    // If the entity killed is from a spawner.
    if (e.entityFromSpawner())
    {
        // Add 50% chance of dropping a coin when mob is from a spawner.
        if (Math.random() <= 0.5)
        {
            ItemStack coin = new Coin().stack(true).item();
            // If you set 'stackCoins' to false in config.yml,
            // only coins dropped by mobs from spawners will stack.

            e.getEntity().getLocation().getWorld().dropItem(e.getEntity().getLocation(), coin);
            // And don't forget to drop the coin.
        }
    }

}