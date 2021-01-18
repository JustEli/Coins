package me.justeli.coins.events;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Extras;
import me.justeli.coins.api.Title;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class DropCoin
        implements Listener
{
    private static final HashMap<Location, Integer> locationTracker = new HashMap<>();

    // Drop coins when mob is killed.
    @EventHandler
    public void ordinaryDeath (EntityDeathEvent e)
    {
        onDeath(e.getEntity(), e.getEntity().getKiller());
    }

    public void onDeath (LivingEntity entity, Player killer)
    {
        for (String world : Settings.hA.get(Config.ARRAY.disabledWorlds))
            if (entity.getWorld().getName().equalsIgnoreCase(world))
                return;

        int setLimit = Settings.hD.get(Config.DOUBLE.limitForLocation).intValue();
        if (setLimit >= 1)
        {
            final Location location = entity.getLocation().getBlock().getLocation().clone();
            int killAmount = locationTracker.getOrDefault(location, 0);
            locationTracker.put(location, killAmount + 1);

            Coins.later(144000, () -> locationTracker.put(location, locationTracker.getOrDefault(location, 0) - 1)); // subtract an hour later

            if (killAmount > setLimit)
                return;
        }

        if (!Settings.hB.get(Config.BOOLEAN.olderServer) && !Settings.hB.get(Config.BOOLEAN.dropWithAnyDeath))
        {
            AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double hitSetting = Settings.hD.get(Config.DOUBLE.percentagePlayerHit);

            if (hitSetting > 0 && maxHealth != null && getPlayerDamage(entity.getUniqueId())/maxHealth.getValue() < hitSetting)
                return;
        }

        if (killer != null)
        {
            if ((entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast || entity instanceof EnderDragon
                    || (!Settings.hB.get(Config.BOOLEAN.olderServer) && entity instanceof Shulker)
                    || (Settings.hB.get(Config.BOOLEAN.newerServer) && entity instanceof Phantom))

                    || ((entity instanceof Animals || entity instanceof Squid || entity instanceof Snowman || entity instanceof IronGolem
                    || entity instanceof Villager || entity instanceof Ambient) && Settings.hB.get(Config.BOOLEAN.passiveDrop))

                    || (entity instanceof Player && Settings.hB.get(Config.BOOLEAN.playerDrop) && Coins.getEconomy().getBalance((Player) entity) >= 0))
            { dropMobCoin(entity, killer); }
        }
        else if (Settings.hB.get(Config.BOOLEAN.dropWithAnyDeath))
            dropMobCoin(entity, null);

        if (entity instanceof Player && Settings.hB.get(Config.BOOLEAN.loseOnDeath))
        {
            double second = Settings.hD.get(Config.DOUBLE.moneyTaken_from);
            double first = Settings.hD.get(Config.DOUBLE.moneyTaken_to) - second;

            Player player = (Player) entity;
            double random = Math.random() * first + second;
            double take = Settings.hB.get(Config.BOOLEAN.takePercentage)? (random / 100) * Coins.getEconomy().getBalance(player) : random;

            if (take > 0 && Coins.getEconomy().withdrawPlayer(player, (long) take).transactionSuccess())
            {
                Title.sendSubTitle(player, 20, 100, 20, Settings.hS.get(Config.STRING.deathMessage).replace("%amount%",
                        String.valueOf((long) take)).replace("{$}", Settings.hS.get(Config.STRING.currencySymbol)));

                if (Settings.hB.get(Config.BOOLEAN.dropOnDeath) && player.getLocation().getWorld() != null)
                    player.getWorld().dropItem(player.getLocation(), new Coin().withdraw((long) take).item());
            }
        }
    }

    // Bow & Trident Section

    @Nullable
    public Projectile resolveProjectileOrNull(EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamager();
        return  (damager instanceof Projectile) ? (Projectile) damager : null;
    }


    @Nullable
    public Player resolvePlayerShooterOrNull(EntityDamageByEntityEvent event)
    {
        Projectile projectile = resolveProjectileOrNull(event);
        if (projectile == null) { return null; }

        ProjectileSource shooter = projectile.getShooter();
        return (shooter instanceof Player) ? (Player) shooter : null;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity dead = event.getEntity();
        EntityDamageEvent damageCause = dead.getLastDamageCause();

        if (!(damageCause instanceof EntityDamageByEntityEvent)) { return; }

        EntityDamageByEntityEvent damageCausedByEntity = (EntityDamageByEntityEvent) damageCause;
        onDeath(dead, resolvePlayerShooterOrNull(damageCausedByEntity));
    }

    // End of Bow & Trident Section

    private void dropMobCoin (Entity m, Player p)
    {
        if (p != null && m instanceof Player && Settings.hB.get(Config.BOOLEAN.preventAlts))
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

        if (!PreventSpawner.fromSpawner(m)
                || (p == null && Settings.hB.get(Config.BOOLEAN.spawnerDrop))
                || (p != null && p.hasPermission("coins.spawner")) )
        {
            if (Math.random() <= Settings.hD.get(Config.DOUBLE.dropChance))
            {
                int amount = 1;
                if (Settings.multiplier.containsKey(m.getType()))
                    amount = Settings.multiplier.get(m.getType());

                dropCoin(amount, p, m.getLocation());
            }
        }
        else
        {
            PreventSpawner.removeFromList(m);
        }
    }

    @EventHandler (ignoreCancelled = true,
                   priority = EventPriority.MONITOR)
    public void onMine (BlockBreakEvent e)
    {
        if (!Settings.hB.get(Config.BOOLEAN.onlyExperienceBlocks))
        {
            dropBlockCoin(e.getBlock(), e.getPlayer());
            return;
        }

        if (e.getExpToDrop() > 0)
            dropBlockCoin(e.getBlock(), e.getPlayer());
    }

    private static void dropBlockCoin (Block block, Player p)
    {
        if (Math.random() <= Settings.hD.get(Config.DOUBLE.minePercentage))
            Coins.later(1, () -> dropCoin(1, p, block.getLocation().clone().add(0.5, 0.5, 0.5)));
    }

    private static void dropCoin (int amount, Player p, Location location)
    {
        if (Settings.hB.get(Config.BOOLEAN.dropEachCoin))
        {
            int second = Settings.hD.get(Config.DOUBLE.moneyAmount_from).intValue();
            int first = Settings.hD.get(Config.DOUBLE.moneyAmount_to).intValue() + 1 - second;

            amount *= (Math.random() * first + second);
        }

        if (p != null)
            amount *= Extras.getMultiplier(p);

        boolean stack = !Settings.hB.get(Config.BOOLEAN.dropEachCoin) && Settings.hB.get(Config.BOOLEAN.stackCoins);
        for (int i = 0; i < amount; i++)
        {
            ItemStack coin = new Coin().stack(stack).item();
            location.getWorld().dropItem(location, coin);
        }
    }

    private static final HashMap<UUID, Double> damages = new HashMap<>();

    private static Double getPlayerDamage (UUID uuid)
    {
        return damages.getOrDefault(uuid, 0D);
    }

    @EventHandler
    public void registerHits (EntityDamageByEntityEvent e)
    {
        if (!(e.getDamager() instanceof Player) || Settings.hB.get(Config.BOOLEAN.olderServer))
            return;

        double playerDamage = damages.getOrDefault(e.getEntity().getUniqueId(), 0D);
        damages.put(e.getEntity().getUniqueId(), playerDamage + e.getFinalDamage());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void unregisterHits (EntityDeathEvent e)
    {
        if (!Settings.hB.get(Config.BOOLEAN.olderServer))
            damages.remove(e.getEntity().getUniqueId());
    }
}
