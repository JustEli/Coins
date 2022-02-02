package me.justeli.coins.config;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Created by Eli on 12/14/2016. Rewritten by Eli on July 9, 2021. */
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
    @ConfigEntry (value = "detectLegacyCoins", motivation = "It is recommended that you add this option to the Coins config as soon as you can, " +
            "AND SET IT TO FALSE. Please note: Keep this option to true if you have withdrawn coins laying around in the server from before Coins " +
            "version 1.11. Also leave the keys 'nameOfCoin' and 'multiSuffix' untouched, if you set 'detectLegacyCoins' to true! Legacy withdrawn " +
            "coins still depend on those two keys!") public static Boolean DETECT_LEGACY_COINS = true; // false in default config
    @ConfigEntry ("allowNameChange") public static Boolean ALLOW_NAME_CHANGE = false;
    @ConfigEntry ("allowModification") public static Boolean ALLOW_MODIFICATION = false;
    @ConfigEntry (value = "checkForUpdates", required = false) public static Boolean CHECK_FOR_UPDATES = true;

    @ConfigEntry ("language") public static String LANGUAGE = "English";
    @ConfigEntry ("coinItem") protected static String RAW_COIN_ITEM = "sunflower";
    @ConfigEntry ("pickupMessage") public static String PICKUP_MESSAGE = "&2+ &a{currency}{amount}";
    @ConfigEntry ("deathMessage") public static String DEATH_MESSAGE = "&4- &c{currency}{amount}";
    @ConfigEntry ("soundName") protected static String RAW_SOUND_NAME = "ITEM_ARMOR_EQUIP_GOLD";
    @ConfigEntry ("currencySymbol") public static String CURRENCY_SYMBOL = "$";
    @ConfigEntry ("skullTexture") public static String SKULL_TEXTURE = "";

    @ConfigEntry (value = "droppedCoinName", motivation = "Has been replaced, previous key was 'nameOfCoin', which will be unsupported in a future " +
            "version.") protected static String RAW_DROPPED_COIN_NAME = "&6Coin";
    @ConfigEntry (value = "withdrawnCoinNames.singular", motivation = "Has been replaced, previous key was 'nameOfCoin', which will be unsupported " +
            "in a future version.") protected static String RAW_WITHDRAWN_COIN_NAME_SINGULAR = "&e{amount} &6Coin";
    @ConfigEntry (value = "withdrawnCoinNames.plural", motivation = "Has been replaced, previous key was 'nameOfCoin' and 'multiSuffix', which will" +
            " be unsupported in a future version.") protected static String RAW_WITHDRAWN_COIN_NAME_PLURAL = "&e{amount} &6Coins";

    @Deprecated @ConfigEntry (value = "nameOfCoin", required = false) protected static String LEGACY_RAW_NAME_OF_COIN = null;
    @Deprecated @ConfigEntry (value = "multiSuffix", required = false) public static String LEGACY_MULTI_SUFFIX = null;
    
    @ConfigEntry ("dropChance") public static Double DROP_CHANCE = 0.9;
    @ConfigEntry ("maxWithdrawAmount") public static Double MAX_WITHDRAW_AMOUNT = 10000.0;
    @ConfigEntry ("moneyAmount.from") public static Double MONEY_AMOUNT_FROM = 3.0;
    @ConfigEntry ("moneyAmount.to") public static Double MONEY_AMOUNT_TO = 7.0;
    @ConfigEntry ("moneyTaken.from") public static Double MONEY_TAKEN_FROM = 10.0;
    @ConfigEntry ("moneyTaken.to") public static Double MONEY_TAKEN_TO = 30.0;
    @ConfigEntry ("minePercentage") public static Double MINE_PERCENTAGE = 0.3;
    @ConfigEntry ("percentagePlayerHit") public static Double PERCENTAGE_PLAYER_HIT = 0.8;
    @ConfigEntry ("enchantIncrement") public static Double ENCHANT_INCREMENT = 0.05;

    @ConfigEntry ("soundPitch") public static Float SOUND_PITCH = 0.3F;
    @ConfigEntry ("soundVolume") public static Float SOUND_VOLUME = 0.5F;
    
    @ConfigEntry ("moneyDecimals") public static Integer MONEY_DECIMALS = 2;
    @ConfigEntry ("limitForLocation") public static Integer LIMIT_FOR_LOCATION = 1;
    @ConfigEntry ("customModelData") public static Integer CUSTOM_MODEL_DATA = 0;
    
    @ConfigEntry ("disabledWorlds") public static Set<String> DISABLED_WORLDS = new HashSet<>();

    @ConfigEntry ("mobMultiplier") protected static Map<String, Integer> RAW_MOB_MULTIPLIER = new HashMap<>();
    @ConfigEntry ("blockMultiplier") protected static Map<String, Integer> RAW_BLOCK_MULTIPLIER = new HashMap<>();

    public static String DROPPED_COIN_NAME;
    public static String WITHDRAWN_COIN_NAME_SINGULAR;
    public static String WITHDRAWN_COIN_NAME_PLURAL;
    @Deprecated public static String LEGACY_WITHDRAWN_COIN_ENDING;

    public static Material COIN_ITEM;
    public static Sound SOUND_NAME;

    protected static final String LEGACY_PREFIX = "&e{amount} &r";

    public static int mobMultiplier (EntityType type)
    {
        return RAW_MOB_MULTIPLIER.computeIfAbsent(type.toString(), empty -> 1);
    }

    public static int blockMultiplier (Material material)
    {
        return RAW_BLOCK_MULTIPLIER.computeIfAbsent(material.toString(), empty -> 1);
    }
}
