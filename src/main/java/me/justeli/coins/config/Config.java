package me.justeli.coins.config;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.config.api.ConfigEntry;
import me.justeli.coins.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
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
    @ConfigEntry ("stackCoins") public static Boolean STACK_COINS = false;
    @ConfigEntry ("spawnerDrop") public static Boolean SPAWNER_DROP = false;
    @ConfigEntry ("passiveDrop") public static Boolean PASSIVE_DROP = false;
    @ConfigEntry ("pickupSound") public static Boolean PICKUP_SOUND = true;
    @ConfigEntry ("loseOnDeath") public static Boolean LOSE_ON_DEATH = true;
    @ConfigEntry ("playerDrop") public static Boolean PLAYER_DROP = true;
    @ConfigEntry ("preventAlts") public static Boolean PREVENT_ALTS = true;
    @ConfigEntry ("enableWithdraw") public static Boolean ENABLE_WITHDRAW = true;
    @ConfigEntry ("dropEachCoin") public static Boolean DROP_EACH_COIN = false;
    @ConfigEntry ("preventSplits") public static Boolean PREVENT_SPLITS = true;
    @ConfigEntry ("takePercentage") public static Boolean TAKE_PERCENTAGE = false;
    @ConfigEntry ("dropOnDeath") public static Boolean DROP_ON_DEATH = false;
    @ConfigEntry ("onlyExperienceBlocks") public static Boolean ONLY_EXPERIENCE_BLOCKS = true;
    @ConfigEntry ("disableHoppers") public static Boolean DISABLE_HOPPERS = false;
    @ConfigEntry ("dropWithAnyDeath") public static Boolean DROP_WITH_ANY_DEATH = false;
    @ConfigEntry ("enchantedCoin") public static Boolean ENCHANTED_COIN = false;
    @ConfigEntry ("disableMythicMobHandling") public static Boolean DISABLE_MYTHIC_MOB_HANDLING = false;

    @ConfigEntry ("language") public static String LANGUAGE = "english";
    @ConfigEntry ("nameOfCoin") private static String RAW__NAME_OF_COIN = "&6Coin";
    @ConfigEntry ("coinItem") private static String RAW__COIN_ITEM = "coin";
    @ConfigEntry ("pickupMessage") public static String PICKUP_MESSAGE = "&2+ &a{$}%amount%";
    @ConfigEntry ("deathMessage") public static String DEATH_MESSAGE = "&4- &c{$}%amount%";
    @ConfigEntry ("soundName") private static String RAW__SOUND_NAME = "ITEM_ARMOR_EQUIP_GOLD";
    @ConfigEntry ("currencySymbol") public static String CURRENCY_SYMBOL = "$";
    @ConfigEntry ("multiSuffix") public static String MULTI_SUFFIX = "s";
    @ConfigEntry ("skullTexture") public static String SKULL_TEXTURE = "";
    
    @ConfigEntry ("dropChance") public static Double DROP_CHANCE = 0.9;
    @ConfigEntry ("maxWithdrawAmount") public static Double MAX_WITHDRAW_AMOUNT = 10000.0;
    @ConfigEntry ("moneyAmount.from") public static Double MONEY_AMOUNT_FROM = 3.0;
    @ConfigEntry ("moneyAmount.to") public static Double MONEY_AMOUNT_TO = 7.0;
    @ConfigEntry ("moneyTaken.from") public static Double MONEY_TAKEN_FROM = 10.0;
    @ConfigEntry ("moneyTaken.to") public static Double MONEY_TAKEN_TO = 30.0;
    @ConfigEntry ("minePercentage") public static Double MINE_PERCENTAGE = 0.3;
    @ConfigEntry ("percentagePlayerHit") public static Double PERCENTAGE_PLAYER_HIT = 0.8;

    @ConfigEntry ("soundPitch") public static Float SOUND_PITCH = 0.3F;
    @ConfigEntry ("soundVolume") public static Float SOUND_VOLUME = 0.5F;
    
    @ConfigEntry ("moneyDecimals") public static Integer MONEY_DECIMALS = 2;
    @ConfigEntry ("limitForLocation") public static Integer LIMIT_FOR_LOCATION = 1;
    @ConfigEntry ("customModelData") public static Integer CUSTOM_MODEL_DATA = 0;
    
    @ConfigEntry ("disabledWorlds") public static Set<String> DISABLED_WORLDS = new HashSet<>();

    @ConfigEntry ("mobMultiplier") private static Map<String, Integer> RAW__MOB_MULTIPLIER = new HashMap<>();
    @ConfigEntry ("blockMultiplier") private static Map<String, Integer> RAW__BLOCK_MULTIPLIER = new HashMap<>();

    public static String NAME_OF_COIN;
    public static Material COIN_ITEM;
    public static Sound SOUND_NAME;

    public static void parse ()
    {
        NAME_OF_COIN = nameOfCoin();
        COIN_ITEM = coinItem();
        SOUND_NAME = soundName();
    }

    public static int mobMultiplier (EntityType type)
    {
        return RAW__MOB_MULTIPLIER.computeIfAbsent(type.toString(), empty -> 1);
    }

    public static int blockMultiplier (Material material)
    {
        return RAW__BLOCK_MULTIPLIER.computeIfAbsent(material.toString(), empty -> 1);
    }

    private static String nameOfCoin ()
    {
        return Util.color(RAW__NAME_OF_COIN);
    }

    private static Material coinItem ()
    {
        String material = RAW__COIN_ITEM
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_")
                .replace("COIN", PaperLib.getMinecraftVersion() > 12? "SUNFLOWER" : "DOUBLE_PLANT");

        try
        {
            return Material.matchMaterial(material);
        }
        catch (IllegalArgumentException exception)
        {
            Config.error("The material '" + RAW__COIN_ITEM + "' in the config at `coinItem` does not exist. Please use a " +
                    "material from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");

            return Material.matchMaterial(PaperLib.getMinecraftVersion() > 12? "SUNFLOWER" : "DOUBLE_PLANT");
        }
    }

    private static Sound soundName ()
    {
        try
        {
            return Sound.valueOf(
                    PaperLib.getMinecraftVersion() < 9 && (RAW__SOUND_NAME.equals("BLOCK_LAVA_POP") || RAW__SOUND_NAME.equals("ITEM_ARMOR_EQUIP_GOLD"))?
                            "NOTE_STICKS"
                            : RAW__SOUND_NAME.toUpperCase()
            );
        }
        catch (IllegalArgumentException exception)
        {
            Config.error("The sound '" + RAW__SOUND_NAME + "' in the config at `soundName` does not exist. Please use a " +
                    "sound from: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");

            // todo test
            return Sound.valueOf(PaperLib.getMinecraftVersion() < 9? "NOTE_STICKS" : "ITEM_ARMOR_EQUIP_GOLD");
        }
    }

    private static int errors = 0;

    public static void error (String message)
    {
        errors ++;
        Coins.plugin().getLogger().log(Level.WARNING, "(Notice " + errors + ") " + message);
    }

    public static void resetErrors ()
    {
        errors = 0;
    }

    public static int getErrors ()
    {
        return errors;
    }
}
