package me.justeli.coins.util;

import io.papermc.lib.PaperLib;
import me.justeli.coins.config.Config;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Wolf;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by Eli on 6 jan. 2020. */
public final class Util
{
    private static final Pattern HEX_PATTERN = Pattern.compile("(?<!\\\\)(&#[a-fA-F0-9]{6})");
    private static final HashMap<UUID, Double> PLAYER_MULTIPLIER = new HashMap<>();
    private static final SplittableRandom RANDOM = new SplittableRandom();

    public static String color (String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', parseRGB(msg));
    }

    public static String formatAmountAndCurrency (String text, double amount)
    {
        String displayAmount = doubleToString(amount);
        return formatCurrency(text.replaceAll("(%amount%|\\{amount})", Matcher.quoteReplacement(displayAmount)));
    }

    public static String formatCurrency (String text)
    {
        // {currency} or {$}
        return text.replaceAll("(\\{currency}|\\{\\$})", Matcher.quoteReplacement(Config.CURRENCY_SYMBOL));
    }

    private static String parseRGB (String msg)
    {
        if (PaperLib.getMinecraftVersion() >= 16)
        {
            Matcher matcher = HEX_PATTERN.matcher(msg);
            while (matcher.find())
            {
                String color = msg.substring(matcher.start(), matcher.end());
                String hex = color.replace("&", "").toUpperCase();

                msg = msg.replace(color, ChatColor.of(hex).toString());
                matcher = HEX_PATTERN.matcher(msg);
            }
        }

        return msg;
    }

    public static void resetMultiplier ()
    {
        PLAYER_MULTIPLIER.clear();
    }

    public static double getMultiplier (Player player)
    {
        if (!PLAYER_MULTIPLIER.containsKey(player.getUniqueId()))
        {
            List<Double> permissions = new ArrayList<>();
            for (PermissionAttachmentInfo permissionInfo : player.getEffectivePermissions())
            {
                String permission = permissionInfo.getPermission();
                if (permission.startsWith("coins.multiplier."))
                {
                    String number = permission.replace("coins.multiplier.", "");
                    permissions.add(Double.parseDouble(number));
                }
            }
            PLAYER_MULTIPLIER.put(player.getUniqueId(), permissions.size() == 0? 1d : Collections.max(permissions));
        }
        return PLAYER_MULTIPLIER.computeIfAbsent(player.getUniqueId(), empty -> 1D);
    }

    public static boolean isHostile (Entity entity)
    {
        return entity instanceof Monster
                || entity instanceof Flying
                || entity instanceof Slime
                || (entity instanceof Golem && !(entity instanceof Snowman))
                || entity instanceof Wolf
                || entity instanceof Boss;
    }

    public static boolean isPassive (Entity entity)
    {
        return !isHostile(entity)
                && !(entity instanceof Player)
                && entity instanceof LivingEntity;
    }

    public static Player getOnlinePlayer (String incomplete)
    {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            if (player.getName().toLowerCase().startsWith(incomplete.toLowerCase()))
            {
                return player;
            }
            else if (player.getDisplayName().toLowerCase().contains(incomplete.toLowerCase()))
            {
                return player;
            }
            else if (player.getName().toLowerCase().contains(incomplete.toLowerCase()))
            {
                return player;
            }
        }
        return null;
    }

    public static void playCoinPickupSound (Player player)
    {
        float volume = Config.SOUND_VOLUME;
        float pitch = Config.SOUND_PITCH;

        Sound sound = Config.SOUND_NAME;
        if (sound == null)
            return;

        player.playSound(player.getEyeLocation(), sound, volume == 0? 0.3f : volume, pitch == 0? 0.3f : pitch);
    }

    public static boolean isDisabledHere (World world)
    {
        return Config.DISABLED_WORLDS.contains(world.getName());
    }

    public static double getRandomMoneyAmount ()
    {
        double second = Config.MONEY_AMOUNT_FROM;
        double first = Config.MONEY_AMOUNT_TO - second;

        return RANDOM.nextDouble() * first + second;
    }

    public static double getRandomTakeAmount ()
    {
        double second = Config.MONEY_TAKEN_FROM;
        double first = Config.MONEY_TAKEN_TO - second;

        return RANDOM.nextDouble() * first + second;
    }

    public static double round (double value)
    {
        return BigDecimal.valueOf(value).setScale(Config.MONEY_DECIMALS, RoundingMode.HALF_UP).doubleValue();
    }

    public static String doubleToString (double input)
    {
        return String.format("%." + Config.MONEY_DECIMALS + "f", round(input));
    }
}