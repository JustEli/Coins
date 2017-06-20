package me.justeli.coins.events;

import me.justeli.coins.api.Title;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.item.Coin;
import me.justeli.coins.main.Coins;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DropCoin implements Listener
{
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
                    (m instanceof Monster || m instanceof Slime || m instanceof MagmaCube || m instanceof Ghast)

                    || ( (m instanceof Animals || m instanceof WaterMob || m instanceof Golem
                            || m instanceof Villager || m instanceof Bat) && Settings.hB.get(Config.BOOLEAN.passiveDrop) )

                    || (m instanceof Player && Settings.hB.get(Config.BOOLEAN.playerDrop) && Coins.getEconomy().getBalance((Player)m) >= 0)
                )

            { dropTheCoin(m, e.getEntity().getKiller()); }

        }

        if ( m instanceof Player && Settings.hB.get(Config.BOOLEAN.loseOnDeath) )
        {
            double second = Settings.hD.get(Config.DOUBLE.moneyTaken_from);
            double first = Settings.hD.get(Config.DOUBLE.moneyTaken_to) - second;

            Player p = (Player) e.getEntity();
            double random = Math.random() * first + second;

            EconomyResponse r = Coins.getEconomy().withdrawPlayer(p, (long) random);
            if (r.transactionSuccess())
                Title.sendSubTitle(p, 20, 100, 20, Settings.hS.get(Config.STRING.deathMessage)
                        .replace("%amount%", String.valueOf( (long)random )).replace("{$}", Settings.hS.get(Config.STRING.currencySymbol)));
        }
	}

	private void dropTheCoin (Entity m, Player p)
    {
        CoinDropEvent dropEvent = new CoinDropEvent(m, p);
        Bukkit.getPluginManager().callEvent(dropEvent);
        if (dropEvent.isCancelled())
            return;

        if (m.getType().equals(EntityType.PLAYER) && Settings.hB.get(Config.BOOLEAN.preventAlts))
        {
            Player player = (Player) m;
            if (p.getAddress().getAddress().getHostAddress()
                    .equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        boolean stack = dropEvent.isStackable();

        if (!PreventSpawner.fromSpawner(m))
        {
            if (Math.random() <= Settings.hD.get(Config.DOUBLE.dropChance))
            {
                ItemStack coin = new Coin().stack(stack).item();
                m.getLocation().getWorld().dropItem(m.getLocation(), coin);

                if (Settings.multiplier.containsKey(m.getType()))
                {
                    int amount = Settings.multiplier.get(m.getType());
                    for (int i = 1; i < amount; i ++)
                    {
                        ItemStack newCoin = new Coin().stack(stack).item();
                        m.getLocation().getWorld().dropItem(m.getLocation(), newCoin);
                    }

                }

            }
        }
        else PreventSpawner.removeFromList(m);
    }

}
