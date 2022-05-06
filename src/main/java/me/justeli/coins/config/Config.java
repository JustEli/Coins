package me.justeli.coins.config;

import me.justeli.coins.util.Util;
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
    @ConfigEntry ("stack-coins") public static Boolean STACK_COINS = false;
    @ConfigEntry ("spawner-drop") public static Boolean SPAWNER_DROP = false;
    @ConfigEntry ("passive-drop") public static Boolean PASSIVE_DROP = false;
    @ConfigEntry ("hostile-drop") public static Boolean HOSTILE_DROP = true;
    @ConfigEntry ("pickup-sound") public static Boolean PICKUP_SOUND = true;
    @ConfigEntry ("lose-on-death") public static Boolean LOSE_ON_DEATH = true;
    @ConfigEntry ("player-drop") public static Boolean PLAYER_DROP = true;
    @ConfigEntry ("prevent-alts") public static Boolean PREVENT_ALTS = true;
    @ConfigEntry ("enable-withdraw") public static Boolean ENABLE_WITHDRAW = true;
    @ConfigEntry ("drop-each-coin") public static Boolean DROP_EACH_COIN = false;
    @ConfigEntry ("prevent-splits") public static Boolean PREVENT_SPLITS = true;
    @ConfigEntry ("take-percentage") public static Boolean TAKE_PERCENTAGE = false;
    @ConfigEntry ("drop-on-death") public static Boolean DROP_ON_DEATH = false;
    @ConfigEntry ("disable-hoppers") public static Boolean DISABLE_HOPPERS = false;
    @ConfigEntry ("drop-with-any-death") public static Boolean DROP_WITH_ANY_DEATH = false;
    @ConfigEntry ("enchanted-coin") public static Boolean ENCHANTED_COIN = false;
    @ConfigEntry ("disable-mythic-mob-handling") public static Boolean DISABLE_MYTHIC_MOB_HANDLING = false;

    @ConfigEntry("levelledmobs-level-multiplier") public static Double LEVELLEDMOBS_LEVEL_MULTIPLIER = 0.0;
    @ConfigEntry (value = "detect-legacy-coins", motivation = "It is recommended that you add this option to the Coins config as soon as you can, " +
            "AND SET IT TO FALSE. Please note: Keep this option to true if you have withdrawn coins laying around in the server from before Coins " +
            "version 1.11. Also leave the keys 'name-of-coin' and 'multi-suffix' untouched, if you set 'detect-legacy-coins' to true! Legacy withdrawn " +
            "coins still depend on those two keys!") public static Boolean DETECT_LEGACY_COINS = true; // false in default config
    @ConfigEntry ("allow-name-change") public static Boolean ALLOW_NAME_CHANGE = false;
    @ConfigEntry ("allow-modification") public static Boolean ALLOW_MODIFICATION = false;
    @ConfigEntry (value = "check-for-updates", required = false) public static Boolean CHECK_FOR_UPDATES = true;

    @ConfigEntry ("language") public static String LANGUAGE = "English";
    @ConfigEntry ("coin-item") public static Material COIN_ITEM = Material.SUNFLOWER;
    @ConfigEntry ("pickup-message") public static String PICKUP_MESSAGE = "&2+ &a{currency}{amount}";
    @ConfigEntry ("death-message") public static String DEATH_MESSAGE = "&4- &c{currency}{amount}";
    @ConfigEntry ("sound-name") public static Sound SOUND_NAME = Sound.ITEM_ARMOR_EQUIP_GOLD;
    @ConfigEntry ("currency-symbol") public static String CURRENCY_SYMBOL = "$";
    @ConfigEntry ("skull-texture") public static String SKULL_TEXTURE = "";

    // todo
    // @ConfigEntry("preferred-economy-hook") public static String PREFERRED_ECONOMY_HOOK = "Vault";
    // # If you have multiple economy handlers installed, which one should get priority?
    // #  This plugin supports either 'Vault' or 'Treasury'.
    // preferred-economy-hook: 'Vault'

    @ConfigEntry (value = "dropped-coin-name", motivation = "This is a replacement, previous key was 'nameOfCoin', which will be unsupported in a future " +
            "version.") public static String DROPPED_COIN_NAME = "&6Coin";
    @ConfigEntry (value = "withdrawn-coin-names.singular", motivation = "This is a replacement, previous key was 'nameOfCoin', which will be unsupported " +
            "in a future version.") public static String WITHDRAWN_COIN_NAME_SINGULAR = "&e{amount} &6Coin";
    @ConfigEntry (value = "withdrawn-coin-names.plural", motivation = "This is a replacement, previous key was 'nameOfCoin' and 'multiSuffix', which will" +
            " be unsupported in a future version.") public static String WITHDRAWN_COIN_NAME_PLURAL = "&e{amount} &6Coins";

    @Deprecated @ConfigEntry (value = "name-of-coin", required = false) protected static String LEGACY_NAME_OF_COIN = null;
    @Deprecated @ConfigEntry (value = "multi-suffix", required = false) protected static String LEGACY_MULTI_SUFFIX = null;
    
    @ConfigEntry ("drop-chance") public static Double DROP_CHANCE = 0.9;
    @ConfigEntry ("max-withdraw-amount") public static Double MAX_WITHDRAW_AMOUNT = 10000.0;
    @ConfigEntry ("money-amount.from") public static Double MONEY_AMOUNT_FROM = 3.0;
    @ConfigEntry ("money-amount.to") public static Double MONEY_AMOUNT_TO = 7.0;
    @ConfigEntry ("money-taken.from") public static Double MONEY_TAKEN_FROM = 10.0;
    @ConfigEntry ("money-taken.to") public static Double MONEY_TAKEN_TO = 30.0;
    @ConfigEntry ("mine-percentage") public static Double MINE_PERCENTAGE = 0.3;
    @ConfigEntry ("percentage-player-hit") public static Double PERCENTAGE_PLAYER_HIT = 0.8;
    @ConfigEntry ("enchant-increment") public static Double ENCHANT_INCREMENT = 0.05;
    @ConfigEntry ("location-limit-hours") public static Double LOCATION_LIMIT_HOURS = 1.0;

    @ConfigEntry ("sound-pitch") public static Float SOUND_PITCH = 0.3F;
    @ConfigEntry ("sound-volume") public static Float SOUND_VOLUME = 0.5F;
    
    @ConfigEntry ("money-decimals") public static Integer MONEY_DECIMALS = 2;
    @ConfigEntry ("limit-for-location") public static Integer LIMIT_FOR_LOCATION = 1;
    @ConfigEntry ("custom-model-data") public static Integer CUSTOM_MODEL_DATA = 0;
    
    @ConfigEntry ("disabled-worlds") public static Set<String> DISABLED_WORLDS = new HashSet<>();

    @ConfigEntry ("mob-multiplier") protected static Map<String, Integer> RAW_MOB_MULTIPLIER = new HashMap<>();

    @ConfigEntry (value = "block-drops", motivation = "New config key that replaces 'block-multiplier'.")
    protected static Map<String, Integer> RAW_BLOCK_DROPS = new HashMap<>();

    @Deprecated @ConfigEntry (value = "block-multiplier", required = false)
    protected static Map<String, Integer> LEGACY_RAW_BLOCK_MULTIPLIER = new HashMap<>();

    @Deprecated public static String LEGACY_WITHDRAWN_COIN_ENDING;

    public static Map<Material, Integer> BLOCK_DROPS = new HashMap<>();
    public static Map<EntityType, Integer> MOB_MULTIPLIER = new HashMap<>();

    @Deprecated protected static final String LEGACY_PREFIX = Util.color("&e{amount} &r");

    private Config () {}
}
