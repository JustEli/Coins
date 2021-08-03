package me.justeli.coins.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.papermc.lib.PaperLib;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Eli on 6 jan. 2020.
 * spigotPlugins: me.justeli.coins.api
 */
public class Skull
{
    private static final HashMap<String, ItemStack> COIN = new HashMap<>();
    private static final UUID SKULL_UUID = UUID.fromString("00000001-0001-0001-0001-000000000002");
    private static final ItemStack SKULL_ITEM;

    static
    {
        if (PaperLib.getMinecraftVersion() >= 13)
        {
            SKULL_ITEM = new ItemStack(Material.PLAYER_HEAD);
        }
        else
        {
            SKULL_ITEM = new ItemStack(Material.matchMaterial("SKULL_ITEM"), 1, (short) 3);
        }
    }

    public static ItemStack of (String texture)
    {
        if (texture == null || texture.isEmpty())
            return null;

        if (COIN.containsKey(texture))
            return COIN.get(texture);

        SkullMeta skullMeta = (SkullMeta) SKULL_ITEM.getItemMeta();

        GameProfile profile = new GameProfile(SKULL_UUID, "randomCoin");
        profile.getProperties().put("textures", new Property("textures", texture));

        Field profileField;

        try
        {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        }
        catch (NoSuchFieldException | SecurityException | NullPointerException e)
        {
            e.printStackTrace();
            return SKULL_ITEM;
        }

        profileField.setAccessible(true);

        try
        {
            profileField.set(skullMeta, profile);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        SKULL_ITEM.setItemMeta(skullMeta);

        COIN.put(texture, SKULL_ITEM);
        return SKULL_ITEM;
    }
}
