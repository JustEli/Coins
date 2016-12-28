package me.justeli.coins.events;

import me.justeli.coins.item.CoinItem;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.settings.LoadSettings;
import me.justeli.coins.settings.Setting;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.justeli.coins.api.Title;

public class DropCoin implements Listener {
	
	private RegisteredServiceProvider<Economy> rep = Bukkit.getServicesManager().getRegistration(Economy.class);
	
	@EventHandler
	public void onDeath (EntityDeathEvent e) {
		
		Entity m = e.getEntity(); // mob

        for (String world : LoadSettings.hA.get(Setting._Array.disabledWorlds) )
            if (m.getWorld().getName().equalsIgnoreCase(world))
                return;

		if (
                ( m instanceof Monster || m instanceof Slime || m instanceof MagmaCube || m instanceof Ghast )
                        && e.getEntity().getKiller() instanceof Player
            )

        { dropTheCoin(m, e.getEntity().getKiller()); }

		else if (
                ( m instanceof Animals || m instanceof WaterMob || m instanceof Golem || m instanceof Villager || m instanceof Bat )
                        && LoadSettings.hB.get(Setting._Boolean.passiveDrop) && e.getEntity().getKiller() instanceof Player
			)

        { dropTheCoin(m, e.getEntity().getKiller()); }
		
		else if (m instanceof Player && LoadSettings.hB.get(Setting._Boolean.loseOnDeath))
		{
			double second = LoadSettings.hD.get(Setting._Double.moneyTaken_from);
			double first = LoadSettings.hD.get(Setting._Double.moneyTaken_to) - second;

			Player p = (Player) e.getEntity();
			double random = Math.random() * first + second;
			
			EconomyResponse r = rep.getProvider().withdrawPlayer(p, (long)random);
			if (r.transactionSuccess()) 
				Title.sendSubTitle(p, 20, 100, 20, "&4&l- &c&l$" + (long)random + " &4&l-");


		}

	}


	private void dropTheCoin (Entity m, Player p)
    {
        CoinDropEvent dropEvent = new CoinDropEvent(m, p);
        Bukkit.getPluginManager().callEvent(dropEvent);
        if (dropEvent.isCancelled())
            return;

        boolean stack = dropEvent.isStackable();

        if (!PreventSpawner.fromSpawner(m))
        {
            if (Math.random() <= LoadSettings.hD.get(Setting._Double.dropChance))
            {
                ItemStack coin = CoinItem.sunflower(stack);
                m.getLocation().getWorld().dropItem(m.getLocation(), coin);
            }
        }
        else PreventSpawner.removeFromList(m);
    }

}
