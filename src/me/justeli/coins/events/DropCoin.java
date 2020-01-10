package me.justeli.coins.events;

import me.justeli.coins.api.Extras;
import me.justeli.coins.api.Title;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.item.Coin;
import me.justeli.coins.main.Coins;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DropCoin implements Listener
{
    // Drop coins when mob is killed.
	@EventHandler
	public void onDeath (EntityDeathEvent e)
    {
		Entity m = e.getEntity();

        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds) )
            if (m.getWorld().getName().equalsIgnoreCase(world))
                return;

        if (e.getEntity().getKiller() != null)
        {
            if (
                    (m instanceof Monster || m instanceof Slime || m instanceof Ghast || m instanceof EnderDragon ||
                            (!Settings.hB.get(Config.BOOLEAN.olderServer) && m instanceof Shulker) ||
                            (Settings.hB.get(Config.BOOLEAN.newerServer) && m instanceof Phantom)
                    )

                    || ( (m instanceof Animals || m instanceof Squid || m instanceof Snowman || m instanceof IronGolem
                            || m instanceof Villager || m instanceof Ambient) && Settings.hB.get(Config.BOOLEAN.passiveDrop) )

                    || (m instanceof Player && Settings.hB.get(Config.BOOLEAN.playerDrop) && Coins.getEconomy().getBalance((Player)m) >= 0)
                )

            { dropMobCoin(m, e.getEntity().getKiller()); }

        }

        if ( m instanceof Player && Settings.hB.get(Config.BOOLEAN.loseOnDeath) )
        {
            double second = Settings.hD.get(Config.DOUBLE.moneyTaken_from);
            double first = Settings.hD.get(Config.DOUBLE.moneyTaken_to) - second;

            Player p = (Player) e.getEntity();
            double random = Math.random() * first + second;
            double take = Settings.hB.get(Config.BOOLEAN.takePercentage)? (random/100)*Coins.getEconomy().getBalance(p) : random;

            if (take > 0 && Coins.getEconomy().withdrawPlayer(p, (long) take).transactionSuccess())
            {
                Title.sendSubTitle(p, 20, 100, 20, Settings.hS.get(Config.STRING.deathMessage)
                        .replace("%amount%", String.valueOf( (long)take )).replace("{$}", Settings.hS.get(Config.STRING.currencySymbol)));

                if (Settings.hB.get(Config.BOOLEAN.dropOnDeath) && p.getLocation().getWorld() != null)
                    p.getWorld().dropItem(p.getLocation(), new Coin().withdraw((long)take).item());
            }
        }
	}

	private void dropMobCoin (Entity m, Player p)
    {
        if (m instanceof Player && Settings.hB.get(Config.BOOLEAN.preventAlts))
        {
            Player player = (Player) m;
            if (p.getAddress().getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        if (PreventSpawner.fromSplit(m))
        {
            PreventSpawner.removeFromList(m);
            return;
        }

        if (!PreventSpawner.fromSpawner(m) || p.hasPermission("coins.spawner"))
        {
            if (Math.random() <= Settings.hD.get(Config.DOUBLE.dropChance))
            {
                int amount = 1;
                if (Settings.multiplier.containsKey(m.getType()))
                    amount = Settings.multiplier.get(m.getType());

                dropCoin(amount, p, m.getLocation());
            }
        }
        else PreventSpawner.removeFromList(m);
    }

    @EventHandler (ignoreCancelled = true)
    public void onMine (BlockBreakEvent e)
    {
        if (!Settings.hB.get(Config.BOOLEAN.onlyExperienceBlocks))
        {
            dropBlockCoin(e.getBlock(), e.getPlayer());
            return;
        }

        if (e.getExpToDrop() != 0)
            dropBlockCoin(e.getBlock(), e.getPlayer());
    }

    private static void dropBlockCoin (Block block, Player p)
    {
        if (Math.random() <= Settings.hD.get(Config.DOUBLE.minePercentage))
            Coins.later(() -> dropCoin (1, p, block.getLocation().clone().add(0.5, 0.5, 0.5)));
    }

    private static void dropCoin (int amount, Player p, Location location)
    {
        if (Settings.hB.get(Config.BOOLEAN.dropEachCoin))
        {
            int second = Settings.hD.get(Config.DOUBLE.moneyAmount_from).intValue();
            int first = Settings.hD.get(Config.DOUBLE.moneyAmount_to).intValue()+1 - second;

            amount *= ( Math.random() * first + second );
        }

        amount *= Extras.getMultiplier(p);

        boolean stack = !Settings.hB.get(Config.BOOLEAN.dropEachCoin) && Settings.hB.get(Config.BOOLEAN.stackCoins);
        for (int i = 0; i < amount; i ++)
        {
            ItemStack coin = new Coin().stack(stack).item();
            location.getWorld().dropItem(location, coin);
        }
    }
}
