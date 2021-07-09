package me.justeli.coins.hook;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.config.Config;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Created by Eli on July 09, 2021.
 * Coins: me.justeli.coins.hook
 */
public class bStatsMetrics
{
    public static void metrics (JavaPlugin plugin, final Consumer<Metric> consumer)
    {
        Metrics metrics = new Metrics(plugin, 831);
        consumer.accept(new Metric(metrics));
    }

    public static class Metric
    {
        private final Metrics metrics;

        public Metric (Metrics metrics)
        {
            this.metrics = metrics;
        }

        public void add (String key, Object value)
        {
            metrics.addCustomChart(new SimplePie(key, value::toString));
        }
    }

    public static void register ()
    {
        metrics(Coins.plugin(), metrics ->
        {
            Sound sound = Config.soundName();

            metrics.add("language", Config.language.toLowerCase(Locale.ROOT));
            metrics.add("currencySymbol", Config.currencySymbol);
            metrics.add("dropChance", Config.dropChance * 100 + "%");
            metrics.add("dropEachCoin", Config.dropEachCoin);
            if (sound != null) metrics.add("pickupSound", sound);
            metrics.add("enableWithdraw", Config.enableWithdraw);
            metrics.add("loseOnDeath", Config.loseOnDeath);
            metrics.add("passiveDrop", Config.passiveDrop);
            metrics.add("nameOfCoin", Config.nameOfCoin());
            metrics.add("coinItem", Config.coinItem());
            metrics.add("pickupMessage", Config.pickupMessage);
            metrics.add("moneyDecimals", Config.moneyDecimals);
            metrics.add("stackCoins", Config.stackCoins);
            metrics.add("playerDrop", Config.playerDrop);
            metrics.add("spawnerDrop", Config.spawnerDrop);
            metrics.add("preventSplits", Config.preventSplits);
            metrics.add("moneyAmount", (Config.moneyAmountFrom + Config.moneyAmountTo) / 2);
            metrics.add("usingSkullTexture", Config.skullTexture != null && !Config.skullTexture.isEmpty());
            metrics.add("disableHoppers", Config.disableHoppers);
            metrics.add("dropWithAnyDeath", Config.dropWithAnyDeath);

            metrics.add("usingPaper", PaperLib.isPaper());
            metrics.add("usingMythicMobs", Coins.hasMythicMobs());
        });
    }
}
