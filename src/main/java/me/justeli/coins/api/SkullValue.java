package me.justeli.coins.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.papermc.lib.PaperLib;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Eli on 6 jan. 2020.
 * spigotPlugins: me.justeli.coins.api
 */
public class SkullValue
{
    // https://github.com/Rocologo/MobHunting

    //  # SKULL: The reward is dropped as a SKULL with a custom texture. You can generate custom texture value
    //  # and custom texture signature at http://mineskin.org

    private static final HashMap<String, ItemStack> coin = new HashMap<>();
    private static final UUID SKULL_UUID = UUID.fromString("00000001-0001-0001-0001-000000000002");

    public static ItemStack get (String texture)
    {
        if (coin.containsKey(texture))
            return coin.get(texture);

        if (texture.isEmpty())
            return null;

        ItemStack skull = PaperLib.getMinecraftVersion() >= 13? new ItemStack(Material.PLAYER_HEAD)
                : new ItemStack(Material.matchMaterial("SKULL_ITEM"), 1, (short) 3);

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

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
            return skull;
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

        skull.setItemMeta(skullMeta);

        coin.put(texture, skull);
        return skull;
    }
}
