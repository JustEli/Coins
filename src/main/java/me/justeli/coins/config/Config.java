package me.justeli.coins.config;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.config.api.ConfigEntry;
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
    @ConfigEntry ("stackCoins") public static Boolean stackCoins = false;
    @ConfigEntry ("spawnerDrop") public static Boolean spawnerDrop = false;
    @ConfigEntry ("passiveDrop") public static Boolean passiveDrop = false;
    @ConfigEntry ("pickupSound") public static Boolean pickupSound = true;
    @ConfigEntry ("loseOnDeath") public static Boolean loseOnDeath = true;
    @ConfigEntry ("playerDrop") public static Boolean playerDrop = true;
    @ConfigEntry ("preventAlts") public static Boolean preventAlts = true;
    @ConfigEntry ("enableWithdraw") public static Boolean enableWithdraw = true;
    @ConfigEntry ("dropEachCoin") public static Boolean dropEachCoin = false;
    @ConfigEntry ("preventSplits") public static Boolean preventSplits = true;
    @ConfigEntry ("takePercentage") public static Boolean takePercentage = false;
    @ConfigEntry ("dropOnDeath") public static Boolean dropOnDeath = false;
    @ConfigEntry ("onlyExperienceBlocks") public static Boolean onlyExperienceBlocks = true;
    @ConfigEntry ("disableHoppers") public static Boolean disableHoppers = false;
    @ConfigEntry ("dropWithAnyDeath") public static Boolean dropWithAnyDeath = false;
    @ConfigEntry ("enchantedCoin") public static Boolean enchantedCoin = false;
    @ConfigEntry ("disableMythicMobHandling") public static Boolean disableMythicMobHandling = false;

    @ConfigEntry ("language") public static String language = "english";
    @ConfigEntry ("nameOfCoin") private static String nameOfCoin = "&6Coin";
    @ConfigEntry ("coinItem") private static String coinItem = "coin";
    @ConfigEntry ("pickupMessage") public static String pickupMessage = "&2+ &a{$}%amount%";
    @ConfigEntry ("deathMessage") public static String deathMessage = "&4- &c{$}%amount%";
    @ConfigEntry ("soundName") private static String soundName = "ITEM_ARMOR_EQUIP_GOLD";
    @ConfigEntry ("currencySymbol") public static String currencySymbol = "$";
    @ConfigEntry ("multiSuffix") public static String multiSuffix = "s";
    @ConfigEntry ("skullTexture") public static String skullTexture = "";
    
    @ConfigEntry ("dropChance") public static Double dropChance = 0.9;
    @ConfigEntry ("maxWithdrawAmount") public static Double maxWithdrawAmount = 10000.0;
    @ConfigEntry ("moneyAmount.from") public static Double moneyAmountFrom = 3.0;
    @ConfigEntry ("moneyAmount.to") public static Double moneyAmountTo = 7.0;
    @ConfigEntry ("moneyTaken.from") public static Double moneyTakenFrom = 10D;
    @ConfigEntry ("moneyTaken.to") public static Double moneyTakenTo = 30D;
    @ConfigEntry ("minePercentage") public static Double minePercentage = 0.3;
    @ConfigEntry ("percentagePlayerHit") public static Double percentagePlayerHit = 0.8;

    @ConfigEntry ("soundPitch") public static Float soundPitch = 0.3F;
    @ConfigEntry ("soundVolume") public static Float soundVolume = 0.5F;
    
    @ConfigEntry ("moneyDecimals") public static Integer moneyDecimals = 2;
    @ConfigEntry ("limitForLocation") public static Integer limitForLocation = 1;
    @ConfigEntry ("customModelData") public static Integer customModelData = 0;
    
    @ConfigEntry ("disabledWorlds") public static Set<String> disabledWorlds;

    @ConfigEntry ("mobMultiplier") private static Map<String, Integer> mobMultiplier;
    @ConfigEntry ("blockMultiplier") private static Map<String, Integer> blockMultiplier;

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
