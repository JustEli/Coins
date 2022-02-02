package me.justeli.coins.hook;

import me.lokka30.treasury.api.economy.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.logging.Level;

/** by Eli on February 01, 2022 **/
public final class Economies implements EconomyHook
{
    private final Plugin plugin;
    private final Set<String> missingPlugins = new LinkedHashSet<>();
    private final Set<String> supportedHooks = new LinkedHashSet<>();

    private EconomyHook hook = null;

    public Economies (Plugin plugin)
    {
        this.plugin = plugin;
        
        hookIfInstalled(TreasuryEconomyHook.TREASURY, treasury ->
            provider(EconomyProvider.class, treasury).map(TreasuryEconomyHook::new)
        );
        
        hookIfInstalled(VaultEconomyHook.VAULT, vault ->
            provider(Economy.class, vault).map(economy -> new VaultEconomyHook(plugin, economy))
        );
        
        if (this.hook == null && missingPlugins.isEmpty())
        {
            this.missingPlugins.add(String.join(" or ", supportedHooks));
        }
    }
    
    private void hookIfInstalled (String name, Function<String, Optional<EconomyHook>> hooker)
    {
        supportedHooks.add(name);
        
        if (this.hook != null) { return; } // already hooked
        if (!plugin.getServer().getPluginManager().isPluginEnabled(name)) { return; }
        
        this.hook = hooker.apply(name).orElse(null);
        
        if (this.hook == null) { missingPlugins.add("an economy providing plugin for '" + name + "'"); }
        else { missingPlugins.clear(); }
    }
    
    private <T> Optional<T> provider (Class<T> economyClass, String name)
    {
        try
        {
            RegisteredServiceProvider<T> registration =
                plugin.getServer().getServicesManager().getRegistration(economyClass);
            
            if (registration == null) { return Optional.empty(); }

            this.plugin.getLogger().log(Level.INFO, name + " is used as the economy provider.");
            return Optional.of(registration.getProvider());
        }
        catch (NullPointerException | NoClassDefFoundError throwable)
        {
            return Optional.empty();
        }
    }

    public Set<String> getMissingPluginNames ()
    {
        return this.missingPlugins;
    }

    @Override
    public void balance (UUID uuid, DoubleConsumer balance)
    {
        if (hook != null) { hook.balance(uuid, balance); }
    }

    @Override
    public void canAfford (UUID uuid, double amount, Consumer<Boolean> canAfford)
    {
        if (hook != null) { hook.canAfford(uuid, amount, canAfford); }
    }

    @Override
    public void withdraw (UUID uuid, double amount, Runnable success)
    {
        if (hook != null) { hook.withdraw(uuid, amount, success); }
    }

    @Override
    public void deposit (UUID uuid, double amount, Runnable success)
    {
        if (hook != null) { hook.deposit(uuid, amount, success); }
    }
}
