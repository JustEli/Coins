package me.justeli.coins.util;

import io.papermc.lib.PaperLib;
import me.justeli.coins.Coins;
import me.justeli.coins.item.Coin;
import me.justeli.coins.config.Config;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eli on 6 jan. 2020.
 * Coins: me.justeli.coins.api
 */
public class Util
{
    private static final Pattern HEX_PATTERN = Pattern.compile("(?<!\\\\)(&#[a-fA-F0-9]{6})");
    private static final HashMap<UUID, Double> PLAYER_MULTIPLIER = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static String color (String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', parseRGB(msg));
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
                || (PaperLib.getMinecraftVersion() >= 14? entity instanceof Boss : entity instanceof EnderDragon);
    }

    public static boolean isPlayer (Entity entity)
    {
        return entity instanceof Player;
    }

    public static boolean isPassive (Entity entity)
    {
        return !isHostile(entity)
                && !isPlayer(entity)
                && entity instanceof LivingEntity;
    }

    public static Player onlinePlayer (String incomplete)
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

    public static boolean isDroppedCoin (ItemStack item)
    {
        if (item == null || item.getItemMeta() == null || !item.getItemMeta().hasDisplayName())
            return false;

        return item.getItemMeta().getDisplayName().equals(Config.NAME_OF_COIN);
    }

    public static boolean isWithdrawnCoin (ItemStack item)
    {
        if (item == null || item.getItemMeta() == null || !item.getItemMeta().hasDisplayName())
            return false;

        return item.getItemMeta().getDisplayName().endsWith(Config.NAME_OF_COIN + Config.MULTI_SUFFIX);
    }

    public static double getWithdrawnTotalWorth (ItemStack item)
    {
        if (!isWithdrawnCoin(item))
            return 0;

        try
        {
            String valuePart = ChatColor.stripColor(item.getItemMeta().getDisplayName().split(" ")[0]);
            return item.getAmount() * new Double(valuePart);
        }
        catch (Exception exception)
        {
            return 0;
        }
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
        for (String disabledWorld : Config.DISABLED_WORLDS)
        {
            if (world.getName().equalsIgnoreCase(disabledWorld) || world.getUID().toString().equalsIgnoreCase(disabledWorld))
            {
                return true;
            }
        }

        return false;
    }

    public static void dropCoins (final Location location, final int radius, final int amount)
    {
        final Location dropLocation = location.clone().add(0.0, 0.5, 0.0);
        final ItemStack coin = new Coin().unique().item();
        final ItemMeta meta = coin.getItemMeta();

        for (int i = 0; i < amount; i++)
        {
            Coins.runLater(i, () ->
            {
                meta.setLore(Collections.singletonList(String.valueOf(RANDOM.nextFloat())));
                coin.setItemMeta(meta);
                Item item = dropLocation.getWorld().dropItem(dropLocation, coin);
                item.setPickupDelay(30);
                item.setVelocity(new Vector(
                        (RANDOM.nextDouble() - 0.5) * radius / 10,
                        RANDOM.nextDouble() * radius / 5,
                        (RANDOM.nextDouble() - 0.5) * radius / 10
                ));
            });
        }
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
}