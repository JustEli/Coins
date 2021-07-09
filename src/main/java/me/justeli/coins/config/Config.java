package me.justeli.coins.config;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.config.api.ConfigFile;
import me.justeli.coins.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by Eli on 12/14/2016.
 * Rewritten by Eli on July 9, 2021.
 */
public class Config
{
    @ConfigFile ("stackCoins") public static Boolean stackCoins = false;
    @ConfigFile ("spawnerDrop") public static Boolean spawnerDrop = false;
    @ConfigFile ("passiveDrop") public static Boolean passiveDrop = false;
    @ConfigFile ("pickupSound") public static Boolean pickupSound = true;
    @ConfigFile ("loseOnDeath") public static Boolean loseOnDeath = true;
    @ConfigFile ("playerDrop") public static Boolean playerDrop = true;
    @ConfigFile ("preventAlts") public static Boolean preventAlts = true;
    @ConfigFile ("enableWithdraw") public static Boolean enableWithdraw = true;
    @ConfigFile ("dropEachCoin") public static Boolean dropEachCoin = false;
    @ConfigFile ("preventSplits") public static Boolean preventSplits = true;
    @ConfigFile ("takePercentage") public static Boolean takePercentage = false;
    @ConfigFile ("dropOnDeath") public static Boolean dropOnDeath = false;
    @ConfigFile ("onlyExperienceBlocks") public static Boolean onlyExperienceBlocks = true;
    @ConfigFile ("disableHoppers") public static Boolean disableHoppers = false;
    @ConfigFile ("dropWithAnyDeath") public static Boolean dropWithAnyDeath = false;
    @ConfigFile ("enchantedCoin") public static Boolean enchantedCoin = false;
    @ConfigFile ("disableMythicMobHandling") public static Boolean disableMythicMobHandling = false;

    @ConfigFile ("language") public static String language = "english";
    @ConfigFile ("nameOfCoin") private static String nameOfCoin = "&6Coin";
    @ConfigFile ("coinItem") private static String coinItem = "coin";
    @ConfigFile ("pickupMessage") public static String pickupMessage = "&2+ &a{$}%amount%";
    @ConfigFile ("deathMessage") public static String deathMessage = "&4- &c{$}%amount%";
    @ConfigFile ("soundName") private static String soundName = "ITEM_ARMOR_EQUIP_GOLD";
    @ConfigFile ("currencySymbol") public static String currencySymbol = "$";
    @ConfigFile ("multiSuffix") public static String multiSuffix = "s";
    @ConfigFile ("skullTexture") public static String skullTexture = "";
    
    @ConfigFile ("dropChance") public static Double dropChance = 0.9;
    @ConfigFile ("maxWithdrawAmount") public static Double maxWithdrawAmount = 10000.0;
    @ConfigFile ("moneyAmount.from") public static Double moneyAmountFrom = 3.0;
    @ConfigFile ("moneyAmount.to") public static Double moneyAmountTo = 7.0;
    @ConfigFile ("moneyTaken.from") public static Double moneyTakenFrom = 10D;
    @ConfigFile ("moneyTaken.to") public static Double moneyTakenTo = 30D;
    @ConfigFile ("minePercentage") public static Double minePercentage = 0.3;
    @ConfigFile ("percentagePlayerHit") public static Double percentagePlayerHit = 0.8;

    @ConfigFile ("soundPitch") public static Float soundPitch = 0.3F;
    @ConfigFile ("soundVolume") public static Float soundVolume = 0.5F;
    
    @ConfigFile ("moneyDecimals") public static Integer moneyDecimals = 2;
    @ConfigFile ("limitForLocation") public static Integer limitForLocation = 1;
    @ConfigFile ("customModelData") public static Integer customModelData = 0;
    
    @ConfigFile ("disabledWorlds") public static Set<String> disabledWorlds;

    @ConfigFile ("mobMultiplier") private static Map<String, Integer> mobMultiplier;
    @ConfigFile ("blockMultiplier") private static Map<String, Integer> blockMultiplier;

    public static int mobMultiplier (EntityType type)
    {
        return mobMultiplier.computeIfAbsent(type.toString(), empty -> 1);
    }

    public static int blockMultiplier (Material material)
    {
        return blockMultiplier.computeIfAbsent(material.toString(), empty -> 1);
    }

    public static String nameOfCoin ()
    {
        return Util.color(nameOfCoin);
    }

    public static Material coinItem ()
    {
        String material = coinItem
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_")
                .replace("COIN", PaperLib.getMinecraftVersion() > 12? "SUNFLOWER" : "DOUBLE_PLANT");

        return Material.matchMaterial(material);
    }

    public static Sound soundName ()
    {
        try
        {
            return Sound.valueOf(
                    PaperLib.getMinecraftVersion() < 9 && (soundName.equals("BLOCK_LAVA_POP") || soundName.equals("ITEM_ARMOR_EQUIP_GOLD"))?
                            "NOTE_STICKS"
                            : soundName.toUpperCase()
            );
        }
        catch (IllegalArgumentException exception)
        {
            Coins.console(Level.WARNING, "The sound '" + soundName + "' does not exist. Change it in the config. Please use a sound " +
                    "from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");

            return null;
        }
    }
}
