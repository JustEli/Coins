package me.justeli.coins.item;

import me.justeli.coins.Coins;
import me.justeli.coins.util.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;

/** by Eli on January 30, 2022 **/
public final class MetaBuilder
        implements Cloneable
{
    private final Coins coins;

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public MetaBuilder (Coins coins, ItemStack itemStack)
    {
        this.coins = coins;
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public MetaBuilder name (String name)
    {
        this.itemMeta.setDisplayName(Util.color(name));
        return this;
    }

    public MetaBuilder data (String key, Integer value)
    {
        this.itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(this.coins, key),
                PersistentDataType.INTEGER, value
        );
        return this;
    }

    public MetaBuilder data (String key, Double value)
    {
        this.itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(this.coins, key),
                PersistentDataType.DOUBLE, value
        );
        return this;
    }

    public <T> Optional<T> data (String key, @NotNull PersistentDataType<T, T> type)
    {
        if (this.itemMeta == null)
            return Optional.empty();

        return Optional.ofNullable(this.itemMeta.getPersistentDataContainer().get(
                new NamespacedKey(this.coins, key), type
        ));
    }

    public ItemStack build ()
    {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }

    @Override
    public MetaBuilder clone ()
    {
        final MetaBuilder clone;
        try
        {
            clone = (MetaBuilder) super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            this.coins.getLogger().log(Level.WARNING, ex.getMessage());
            return new MetaBuilder(this.coins, build().clone());
        }
        return clone;
    }
}
