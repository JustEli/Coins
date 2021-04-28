package me.justeli.coins.events;

import me.justeli.coins.Coins;
import me.justeli.coins.api.Extras;
import me.justeli.coins.api.IsEntity;
import me.justeli.coins.api.Title;
import me.justeli.coins.cancel.PreventSpawner;
import me.justeli.coins.item.Coin;
import me.justeli.coins.settings.Config;
import me.justeli.coins.settings.Settings;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
import java.util.Random;
import java.util.UUID;

public class DropCoin
        implements Listener
{
    private static final HashMap<Location, Integer> locationTracker = new HashMap<>();

    // Drop coins when mob is killed.
    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityDeath (EntityDeathEvent event)
    {
        if (Coins.isDisabled())
            return;

        LivingEntity dead = event.getEntity();
        EntityDamageEvent damageCause = dead.getLastDamageCause();

        if (dead.getKiller() != null)
        {
            entityDeath(event.getEntity(), event.getEntity().getKiller());
        }
        else if (damageCause instanceof EntityDamageByEntityEvent)
        {
            entityDeath(dead, resolvePlayerShooterOrNull((EntityDamageByEntityEvent) damageCause));
        }
        else
        {
            entityDeath(dead, null);
        }

        PreventSpawner.removeFromList(dead);
    }

    public void entityDeath (LivingEntity entity, Player killer)
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

        if (!Settings.hB.get(Config.BOOLEAN.olderServer) && !Settings.hB.get(Config.BOOLEAN.dropWithAnyDeath) && killer != null)
        {
            AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double hitSetting = Settings.hD.get(Config.DOUBLE.percentagePlayerHit);

            if (hitSetting > 0 && maxHealth != null && getPlayerDamage(entity.getUniqueId()) / maxHealth.getValue() < hitSetting)
                return;
        }

        if (killer != null)
        {
            if (
                    IsEntity.hostile(entity) ||
                    (IsEntity.passive(entity) && Settings.hB.get(Config.BOOLEAN.passiveDrop)) ||
                    (IsEntity.player(entity) && Settings.hB.get(Config.BOOLEAN.playerDrop) && Coins.getEconomy().getBalance((Player) entity) >= 0)
            )
            {
                dropMobCoin(entity, killer);
            }
        }
        else if (Settings.hB.get(Config.BOOLEAN.dropWithAnyDeath) && entity instanceof Mob)
        {
            dropMobCoin(entity, null);
        }

        if (IsEntity.player(entity) && Settings.hB.get(Config.BOOLEAN.loseOnDeath))
        {
            double second = Settings.hD.get(Config.DOUBLE.moneyTaken_from);
            double first = Settings.hD.get(Config.DOUBLE.moneyTaken_to) - second;

            Player player = (Player) entity;
            double random = RANDOM.nextDouble() * first + second;
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
        if (projectile == null)
            return null;

        ProjectileSource shooter = projectile.getShooter();
        return (shooter instanceof Player) ? (Player) shooter : null;
    }

    // End of Bow & Trident Section

    private void dropMobCoin (Entity victim, Player killer)
    {
        if (killer != null && victim instanceof Player && Settings.hB.get(Config.BOOLEAN.preventAlts))
        {
            Player player = (Player) victim;
            if (killer.getAddress().getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        if (PreventSpawner.fromSplit(victim))
            return;

        if (!PreventSpawner.fromSpawner(victim)
                || (killer == null && Settings.hB.get(Config.BOOLEAN.spawnerDrop))
                || (killer != null && killer.hasPermission("coins.spawner")) )
        {
            if (RANDOM.nextDouble() <= Settings.hD.get(Config.DOUBLE.dropChance))
            {
                final int amount = Settings.mobMultipliers.computeIfAbsent(victim.getType(), empty -> 1);
                dropCoin(amount, killer, victim.getLocation());
            }
        }
    }

    @EventHandler (ignoreCancelled = true,
                   priority = EventPriority.MONITOR)
    public void onMine (BlockBreakEvent event)
    {
        if (Coins.isDisabled())
            return;

        if (!Settings.hB.get(Config.BOOLEAN.onlyExperienceBlocks))
        {
            dropBlockCoin(event.getBlock(), event.getPlayer());
            return;
        }

        if (event.getExpToDrop() > 0)
            dropBlockCoin(event.getBlock(), event.getPlayer());
    }

    private static final Random RANDOM = new Random();

    private void dropBlockCoin (Block block, Player player)
    {
        if (RANDOM.nextDouble() <= Settings.hD.get(Config.DOUBLE.minePercentage))
        {
            final int amount = Settings.blockMultipliers.computeIfAbsent(block.getType(), empty -> 1);
            Coins.later(1, () -> dropCoin(amount, player, block.getLocation().clone().add(0.5, 0.5, 0.5)));
        }
    }

    private void dropCoin (int amount, Player player, Location location)
    {
        if (Settings.hB.get(Config.BOOLEAN.dropEachCoin))
        {
            int second = Settings.hD.get(Config.DOUBLE.moneyAmount_from).intValue();
            int first = Settings.hD.get(Config.DOUBLE.moneyAmount_to).intValue() + 1 - second;

            amount *= RANDOM.nextDouble() * first + second;
        }

        if (player != null)
            amount *= Extras.getMultiplier(player);

        boolean stack = !Settings.hB.get(Config.BOOLEAN.dropEachCoin) && Settings.hB.get(Config.BOOLEAN.stackCoins);

        if (location.getWorld() == null)
            return;

        for (int i = 0; i < amount; i++)
        {
            ItemStack coin = new Coin().stack(stack).item();
            location.getWorld().dropItem(location, coin);
        }
    }

    private static final HashMap<UUID, Double> damages = new HashMap<>();

    private Double getPlayerDamage (UUID uuid)
    {
        return damages.computeIfAbsent(uuid, empty -> 0D);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void registerHits (EntityDamageByEntityEvent event)
    {
        if (Settings.hB.get(Config.BOOLEAN.olderServer))
            return;

        if (!(event.getDamager() instanceof Player) && resolvePlayerShooterOrNull(event) == null)
            return;

        UUID uuid = event.getEntity().getUniqueId();
        double playerDamage = damages.computeIfAbsent(uuid, empty -> 0D);
        damages.put(uuid, playerDamage + event.getFinalDamage());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void unregisterHits (EntityDeathEvent event)
    {
        damages.remove(event.getEntity().getUniqueId());
    }
}
