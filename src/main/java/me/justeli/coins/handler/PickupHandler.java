package me.justeli.coins.handler;

import me.justeli.coins.Coins;
import me.justeli.coins.event.PickupEvent;
import me.justeli.coins.item.CoinUtil;
import me.justeli.coins.util.ActionBar;
import me.justeli.coins.config.Config;
import me.justeli.coins.util.Util;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PickupHandler
        implements Listener
{
    private final Coins coins;

    public PickupHandler (Coins coins)
    {
        this.coins = coins;
    }

    private static final Set<UUID> THROWN_COINS = new HashSet<>();
    private static final HashMap<UUID, Double> PICKUP_AMOUNT_TRACKER = new HashMap<>();

    @EventHandler (ignoreCancelled = true)
    public void onPickup (PickupEvent event)
    {
        if (Util.isDisabledHere(event.getPlayer().getWorld()))
            return;

        Item item = event.getItem();
        if (this.coins.getCoinUtil().isCoin(item.getItemStack()))
        {
            Player player = event.getPlayer();
            event.setCancelled(true);

            if (!player.hasPermission("coins.disable") || player.isOp() || player.hasPermission("*"))
            {
                double amount = this.coins.getCoinUtil().getValue(item.getItemStack());
                giveCoin(item, player, amount);
            }
        }
    }

    private void giveCoin (Item item, Player player, double randomMoney)
    {
        if (THROWN_COINS.contains(item.getUniqueId()))
            return;

        THROWN_COINS.add(item.getUniqueId());
        item.setVelocity(new Vector(0, 0.4, 0));

        this.coins.sync(5, () ->
        {
            item.remove();
            THROWN_COINS.remove(item.getUniqueId());
        });

        // pass 0 for random amount
        if (randomMoney == 0)
        {
            giveRandomMoney(item.getItemStack(), player);
        }
        else
        {
            giveMoney(player, randomMoney);
        }

        if (Config.PICKUP_SOUND)
        {
            Util.playCoinPickupSound(player);
        }
    }

    public void giveRandomMoney (ItemStack item, Player player)
    {
        if (Config.DROP_EACH_COIN)
        {
            giveMoney(player, item.getAmount());
            return;
        }

        int amount = item.getAmount();
        double total = amount * Util.getRandomMoneyAmount();

        giveMoney(player, total);
    }

    public void giveMoney (Player player, double rawAmount)
    {
        final double amount = Util.round(rawAmount);
        this.coins.economy().deposit(player.getUniqueId(), amount, () ->
        {
            UUID uniqueId = player.getUniqueId();

            double previousAmount = PICKUP_AMOUNT_TRACKER.computeIfAbsent(uniqueId, empty -> 0D);
            PICKUP_AMOUNT_TRACKER.put(uniqueId, amount + previousAmount);
            final double displayAmount = PICKUP_AMOUNT_TRACKER.computeIfAbsent(uniqueId, empty -> 0D);

            this.coins.sync(Config.DROP_EACH_COIN? 30 : 10, () ->
            {
                if (PICKUP_AMOUNT_TRACKER.computeIfAbsent(uniqueId, empty -> 0D) == displayAmount)
                {
                    PICKUP_AMOUNT_TRACKER.remove(uniqueId);
                }
            });

            ActionBar.of(Util.formatAmountAndCurrency(Config.PICKUP_MESSAGE, displayAmount)).send(player);
        });
    }
}
