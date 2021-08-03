package me.justeli.coins.handler;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.hook.MythicMobsHook;
import me.justeli.coins.item.Coin;
import me.justeli.coins.config.Config;
import me.justeli.coins.util.SubTitle;
import me.justeli.coins.util.Util;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

public class DropHandler
        implements Listener
{
    private static final HashMap<Location, Integer> LOCATION_TRACKER = new HashMap<>();
    private static final HashMap<UUID, Double> DAMAGES = new HashMap<>();
    private static final Random RANDOM = new Random();

    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityDeath (EntityDeathEvent event)
    {
        if (Coins.isDisabled())
            return;

        LivingEntity dead = event.getEntity();

        if (Util.isDisabledHere(dead.getWorld()))
            return;

        if (Coins.hasMythicMobs() && Config.DISABLE_MYTHIC_MOB_HANDLING && MythicMobsHook.isMythicMob(dead))
            return;

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
    }

    public void entityDeath (LivingEntity entity, Player killer)
    {
        if (Config.LIMIT_FOR_LOCATION >= 1)
        {
            final Location location = entity.getLocation().getBlock().getLocation().clone();
            int killAmount = LOCATION_TRACKER.getOrDefault(location, 0);
            LOCATION_TRACKER.put(location, killAmount + 1);

            // todo improve
            // subtract an hour later
            Coins.runLater(144000, () -> LOCATION_TRACKER.put(location, LOCATION_TRACKER.getOrDefault(location, 0) - 1));

            if (killAmount > Config.LIMIT_FOR_LOCATION)
                return;
        }

        if (PaperLib.getMinecraftVersion() >= 10 && !Config.DROP_WITH_ANY_DEATH && killer != null)
        {
            AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double hitSetting = Config.PERCENTAGE_PLAYER_HIT;

            if (hitSetting > 0 && maxHealth != null && getPlayerDamage(entity.getUniqueId()) / maxHealth.getValue() < hitSetting)
                return;
        }

        if (killer != null)
        {
            if (Util.isHostile(entity)
                    || (Util.isPassive(entity) && Config.PASSIVE_DROP)
                    || (Util.isPlayer(entity) && Config.PLAYER_DROP && Coins.economy().getBalance((Player) entity) >= 0))
            {
                dropMobCoin(entity, killer);
            }
        }
        else if (Config.DROP_WITH_ANY_DEATH)
        {
            dropMobCoin(entity, null);
        }

        if (Util.isPlayer(entity) && Config.LOSE_ON_DEATH)
        {
            Player player = (Player) entity;

            double random = Util.getRandomTakeAmount();
            double take = Util.round(Config.TAKE_PERCENTAGE? (random / 100) * Coins.economy().getBalance(player) : random);

            if (take > 0 && Coins.economy().withdrawPlayer(player, take).transactionSuccess())
            {
                SubTitle.of(Config.DEATH_MESSAGE.replace("%amount%", Util.doubleToString(take))).send(player);

                if (Config.DROP_ON_DEATH && player.getLocation().getWorld() != null)
                {
                    player.getWorld().dropItem(player.getLocation(), new Coin().withdraw(take).item());
                }
            }
        }
    }

    // Bow & Trident Section

    @Nullable
    public Projectile resolveProjectileOrNull (EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamager();
        return (damager instanceof Projectile)? (Projectile) damager : null;
    }

    @Nullable
    public Player resolvePlayerShooterOrNull (EntityDamageByEntityEvent event)
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
        if (killer != null && Util.isPlayer(victim) && Config.PREVENT_ALTS)
        {
            Player player = (Player) victim;
            if (killer.getAddress().getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        if (UnfairMobHandler.fromSplit(victim) && Config.PREVENT_SPLITS)
            return;

        if (UnfairMobHandler.fromSpawner(victim)
                && (killer != null || !Config.SPAWNER_DROP)
                && (killer == null || !killer.hasPermission("coins.spawner")))
            return;

        if (RANDOM.nextDouble() <= Config.DROP_CHANCE)
        {
            final int amount = Config.mobMultiplier(victim.getType());
            dropCoin(amount, killer, victim.getLocation());
        }
    }

    @EventHandler (ignoreCancelled = true,
                   priority = EventPriority.MONITOR)
    public void onMine (BlockBreakEvent event)
    {
        if (Coins.isDisabled())
            return;

        if (!Config.ONLY_EXPERIENCE_BLOCKS)
        {
            dropBlockCoin(event.getBlock(), event.getPlayer());
            return;
        }

        if (event.getExpToDrop() > 0)
        {
            dropBlockCoin(event.getBlock(), event.getPlayer());
        }
    }

    private void dropBlockCoin (Block block, Player player)
    {
        if (RANDOM.nextDouble() <= Config.MINE_PERCENTAGE)
        {
            final int amount = Config.blockMultiplier(block.getType());
            Coins.runLater(1, () -> dropCoin(amount, player, block.getLocation().clone().add(0.5, 0.5, 0.5)));
        }
    }

    // todo improve
    private void dropCoin (int amount, Player player, Location location)
    {
        if (Config.DROP_EACH_COIN)
        {
            // todo ??
            int second = (int) Config.MONEY_AMOUNT_FROM.doubleValue();
            int first = ((int) Config.MONEY_AMOUNT_TO.doubleValue()) + 1 - second;

            amount *= RANDOM.nextDouble() * first + second;
        }

        if (player != null)
        {
            amount *= Util.getMultiplier(player);
        }

        boolean stack = !Config.DROP_EACH_COIN && Config.STACK_COINS;

        if (location.getWorld() == null)
            return;

        for (int i = 0; i < amount; i++)
        {
            ItemStack coin = new Coin().stack(stack).item();
            location.getWorld().dropItem(location, coin);
        }
    }

    private double getPlayerDamage (UUID uuid)
    {
        return DAMAGES.computeIfAbsent(uuid, empty -> 0D);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void registerHits (EntityDamageByEntityEvent event)
    {
        // getAttribute (line 88) not working < 1.10
        if (PaperLib.getMinecraftVersion() < 10)
            return;

        if (!(event.getDamager() instanceof Player) && resolvePlayerShooterOrNull(event) == null)
            return;

        UUID uuid = event.getEntity().getUniqueId();
        double playerDamage = DAMAGES.computeIfAbsent(uuid, empty -> 0D);
        DAMAGES.put(uuid, playerDamage + event.getFinalDamage());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void unregisterHits (EntityDeathEvent event)
    {
        DAMAGES.remove(event.getEntity().getUniqueId());
    }
}
