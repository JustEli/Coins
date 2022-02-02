package me.justeli.coins.handler;

import me.justeli.coins.Coins;
import me.justeli.coins.hook.MythicMobsHook;
import me.justeli.coins.config.Config;
import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.item.CreateCoin;
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
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.SplittableRandom;
import java.util.UUID;

public class DropHandler
        implements Listener
{
    private final Coins coins;

    public DropHandler (Coins coins)
    {
        this.coins = coins;
    }

    private static final HashMap<Location, Integer> LOCATION_TRACKER = new HashMap<>();
    private static final HashMap<UUID, Double> DAMAGES = new HashMap<>();
    private static final SplittableRandom RANDOM = new SplittableRandom();

    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityDeath (EntityDeathEvent event)
    {
        if (this.coins.isDisabled())
            return;

        LivingEntity dead = event.getEntity();

        if (Util.isDisabledHere(dead.getWorld()))
            return;

        if (this.coins.hasMythicMobs() && Config.DISABLE_MYTHIC_MOB_HANDLING && MythicMobsHook.isMythicMob(dead))
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
            this.coins.sync(144000, () -> LOCATION_TRACKER.put(location, LOCATION_TRACKER.getOrDefault(location, 0) - 1));

            if (killAmount > Config.LIMIT_FOR_LOCATION)
                return;
        }

        if (!Config.DROP_WITH_ANY_DEATH && killer != null)
        {
            AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double hitSetting = Config.PERCENTAGE_PLAYER_HIT;

            if (hitSetting > 0 && maxHealth != null && getPlayerDamage(entity.getUniqueId()) / maxHealth.getValue() < hitSetting)
                return;
        }

        if (killer != null)
        {
            if (Util.isHostile(entity) || (Util.isPassive(entity) && Config.PASSIVE_DROP))
            {
                dropMobCoin(entity, killer);
            }
            else if (entity instanceof Player && Config.PLAYER_DROP)
            {
                this.coins.economy().balance(entity.getUniqueId(), balance ->
                {
                    if (balance > 0)
                    {
                        dropMobCoin(entity, killer);
                    }
                });
            }
        }
        else if (Config.DROP_WITH_ANY_DEATH)
        {
            dropMobCoin(entity, null);
        }

        if (entity instanceof Player && Config.LOSE_ON_DEATH)
        {
            Player player = (Player) entity;

            double random = Util.getRandomTakeAmount();

            this.coins.economy().balance(player.getUniqueId(), balance ->
            {
                if (balance <= 0)
                    return;

                double take = Util.round(
                        Config.TAKE_PERCENTAGE
                                ? (random / 100) * balance
                                : random
                );

                if (take > 0)
                {
                    this.coins.economy().withdraw(player.getUniqueId(), take, () ->
                    {
                        SubTitle.of(Util.formatAmountAndCurrency(Config.DEATH_MESSAGE, take)).send(player);

                        if (Config.DROP_ON_DEATH && player.getLocation().getWorld() != null)
                        {
                            player.getWorld().dropItem(
                                    player.getLocation(),
                                    this.coins.getCreateCoin().other().data(CoinUtil.COINS_WORTH, take).build()
                            );
                        }
                    });
                }
            });
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
        if (killer != null && victim instanceof Player && Config.PREVENT_ALTS)
        {
            Player player = (Player) victim;
            if (killer.getAddress().getAddress().getHostAddress().equals(player.getAddress().getAddress().getHostAddress()))
                return;
        }

        if (this.coins.getUnfairMobHandler().fromSplit(victim) && Config.PREVENT_SPLITS)
            return;

        if (this.coins.getUnfairMobHandler().fromSpawner(victim) && !hasSpawnerPermission(killer))
            return;

        if (RANDOM.nextDouble() <= Config.DROP_CHANCE)
        {
            final int amount = Config.mobMultiplier(victim.getType());
            dropCoin(amount, killer, victim.getLocation());
        }
    }

    private boolean hasSpawnerPermission (Player player)
    {
        if (player == null)
            return Config.SPAWNER_DROP;

        return Config.SPAWNER_DROP || player.hasPermission("coins.spawner");
    }

    @EventHandler (ignoreCancelled = true,
                   priority = EventPriority.MONITOR)
    public void onMine (BlockBreakEvent event)
    {
        if (this.coins.isDisabled())
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
            this.coins.sync(1, () -> dropCoin(amount, player, block.getLocation().clone().add(0.5, 0.5, 0.5)));
        }
    }

    private void dropCoin (int amount, Player player, Location location)
    {
        if (Config.DROP_EACH_COIN)
        {
            int second = Config.MONEY_AMOUNT_FROM.intValue();
            int first = Config.MONEY_AMOUNT_TO.intValue() + 1 - second;

            amount *= RANDOM.nextDouble() * first + second;
        }

        if (player != null)
        {
            amount *= Util.getMultiplier(player);
        }

        if (location.getWorld() == null)
            return;

        for (int i = 0; i < amount; i++)
        {
            location.getWorld().dropItem(location, this.coins.getCreateCoin().dropped());
        }
    }

    private double getPlayerDamage (UUID uuid)
    {
        return DAMAGES.computeIfAbsent(uuid, empty -> 0D);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void registerHits (EntityDamageByEntityEvent event)
    {
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
