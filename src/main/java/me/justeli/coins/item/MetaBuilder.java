package me.justeli.coins.item;

import me.justeli.coins.Coins;
import me.justeli.coins.util.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** by Eli on January 30, 2022 **/
public class MetaBuilder
{
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    private MetaBuilder (ItemStack itemStack)
    {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public static MetaBuilder of (@NotNull ItemStack itemStack)
    {
        return new MetaBuilder(itemStack);
    }

    public MetaBuilder name (String name)
    {
        this.itemMeta.setDisplayName(Util.color(name));
        return this;
    }

    public MetaBuilder data (String key, Integer value)
    {
        this.itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(Coins.plugin(), key),
                PersistentDataType.INTEGER, value
        );
        return this;
    }

    public MetaBuilder data (String key, Double value)
    {
        this.itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(Coins.plugin(), key),
                PersistentDataType.DOUBLE, value
        );
        return this;
    }

    public <T> Optional<T> data (String key, @NotNull PersistentDataType<T, T> type)
    {
        if (this.itemMeta == null)
            return Optional.empty();

        return Optional.ofNullable(this.itemMeta.getPersistentDataContainer().get(
                new NamespacedKey(Coins.plugin(), key), type
        ));
    }

    public ItemStack build ()
    {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }
}
