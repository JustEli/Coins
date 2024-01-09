package me.justeli.coins.hook;

import me.justeli.coins.hook.vault.VaultEconomyHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/* Eli @ February 01, 2022 (creation) */
public final class Economies
    implements EconomyHook
{
    private final Plugin plugin;
    private final Set<String> missingPlugins = new LinkedHashSet<>();
    private final Set<String> supportedHooks = new LinkedHashSet<>();

    private EconomyHook economy = null;

    public Economies (Plugin plugin)
    {
        this.plugin = plugin;
        
        hookIfInstalled(VaultEconomyHook.VAULT, () ->
            Optional.ofNullable(plugin.getServer().getServicesManager().getRegistration(Economy.class))
                .map(registration -> new VaultEconomyHook(plugin, registration.getProvider()))
        );
        
        if (this.economy == null && this.missingPlugins.isEmpty())
        {
            this.missingPlugins.add(String.join(" or ", this.supportedHooks));
        }
    }
    
    private void hookIfInstalled (String name, Supplier<Optional<EconomyHook>> hooker)
    {
        this.supportedHooks.add(name);
        
        if (this.economy != null) { return; } // already hooked
        if (!plugin.getServer().getPluginManager().isPluginEnabled(name)) { return; }
        
        try { this.economy = hooker.get().orElse(null); }
        catch (NullPointerException | NoClassDefFoundError ignored) {}
        
        if (this.economy == null)
        {
            missingPlugins.add("an economy providing plugin for '" + name + "'");
        }
        else
        {
            this.plugin.getLogger().log(Level.INFO, name + " is used as the economy provider.");
            missingPlugins.clear();
        }
    }

    public Set<String> getMissingPluginNames ()
    {
        return this.missingPlugins;
    }

    @Override
    public void balance (UUID uuid, DoubleConsumer balance)
    {
        if (economy != null) { economy.balance(uuid, balance); }
    }

    @Override
    public void canAfford (UUID uuid, double amount, Consumer<Boolean> canAfford)
    {
        if (economy != null) { economy.canAfford(uuid, amount, canAfford); }
    }

    @Override
    public void withdraw (UUID uuid, double amount, Runnable success)
    {
        if (economy != null) { economy.withdraw(uuid, amount, success); }
    }

    @Override
    public void deposit (UUID uuid, double amount, Runnable success)
    {
        if (economy != null) { economy.deposit(uuid, amount, success); }
    }

    @Override
    public Optional<String> name ()
    {
        if (this.economy == null)
            return Optional.empty();

        return this.economy.name();
    }
}
