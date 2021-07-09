![](https://img.shields.io/bstats/players/831?style=flat-square)
![](https://img.shields.io/bstats/servers/831?style=flat-square)
![](https://img.shields.io/spiget/download-size/33382?style=flat-square)
![](https://img.shields.io/spiget/downloads/33382?style=flat-square)
![](https://img.shields.io/spiget/rating/33382?style=flat-square)
![](https://img.shields.io/github/v/release/JustEli/Coins?style=flat-square)
![](https://img.shields.io/github/commit-activity/m/JustEli/Coins?style=flat-square)
![](https://img.shields.io/github/contributors/JustEli/Coins?style=flat-square)
![](https://img.shields.io/github/last-commit/JustEli/Coins/master?style=flat-square)
![](https://img.shields.io/github/issues-raw/JustEli/Coins?style=flat-square)
![](https://img.shields.io/github/issues-closed-raw/JustEli/Coins?style=flat-square)

# Coins
Configurable lightweight coins for killing and mining, with a withdraw system and perfect for drop parties.
- Official Spigot page: https://www.spigotmc.org/resources/coins.33382/
- Donate to the developer: https://paypal.me/justeli
- See it live: join the developer's server at `survival.rocks`.


## Small and simple API for Coins
*May be slightly outdated, I don't think most is working anymore.*

Coins has three parts in the plugin usable for outside developers. The classes: Coins, Coin, ~~CoinDropEvent~~. 

You can drop coins in a radius around someone.

```java
import me.justeli.coins.command.Coins;
Coins.particles(Location location,int radius,int amount);
```

So you could, for example, do a coin rain on advancement get (outdated event).
```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

@EventHandler
public void advancementRain (PlayerAchievementAwardedEvent e)
{
    Coins.particles (e.getPlayer().getLocation(), 3, 20);
}
```
This drops 20 coins in a radius of 3 around the specific player.

You can create a coin, too.
```java
import me.justeli.coins.item.Coin;
ItemStack coin = new Coin().item();                 // gets the ItemStack of one coin
ItemStack coin = new Coin().stack(false).item();    // creates coin, but not stackable
ItemStack coin = new Coin().withdraw(100).item();   // makes it a withdrawn coin
```

When 'spawnerDrop' is set to false (default), you can check if a mob is spawned by a spawner. Since every mob from a spawner has a hashmap (if 'spawnerDrop' is set to false).

```java
import me.justeli.coins.command.Coins;
boolean mobFromSpawner=Coins.mobFromSpawner(Entity entity);
```
Returns true if the Entity is from a spawner, false if not.

The plugin has its own event, too!
```java
import me.justeli.coins.events.CoinDropEvent;

@EventHandler
public void onCoinDrop (CoinDropEvent e)
{
    if (e.getKiller().hasPermission("no.coins.from.skeletons") && e.getEntityType().equals(EntityType.SKELETON))
    {
        e.setCancelled( true );
    }
```
When the player has the permission 'no.coins.from.skeletons', and the entity killed is a skeleton, it won't drop a coin.

The following enables coin drops for mobs from spawners when 'spawnerDrop' is set to false in config. But you can do more advanced stuff, since the plugin can only detect mobs from a spawner if 'spawnerDrop' is set to false. If the entity killed is from a spawner.
```java
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
```
