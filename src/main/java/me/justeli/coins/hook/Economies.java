package me.justeli.coins.hook;

import me.lokka30.treasury.api.common.service.Service;
import me.lokka30.treasury.api.common.service.ServiceRegistry;
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
            ServiceRegistry.INSTANCE.serviceFor(EconomyProvider.class)
                .map(service -> new TreasuryEconomyHook(service.get()))
        );
        
        hookIfInstalled(VaultEconomyHook.VAULT, vault ->
        {
            try
            {
                RegisteredServiceProvider<Economy> registration =
                    plugin.getServer().getServicesManager().getRegistration(Economy.class);
                
                if (registration == null) { return Optional.empty(); }
                
                return Optional.of(new VaultEconomyHook(plugin, registration.getProvider()));
            }
            catch (NullPointerException | NoClassDefFoundError throwable)
            {
                return Optional.empty();
            }
        });
        
        if (this.hook == null && this.missingPlugins.isEmpty())
        {
            this.missingPlugins.add(String.join(" or ", this.supportedHooks));
        }
    }
    
    private void hookIfInstalled (String name, Function<String, Optional<EconomyHook>> hooker)
    {
        this.supportedHooks.add(name);
        
        if (this.hook != null) { return; } // already hooked
        if (!plugin.getServer().getPluginManager().isPluginEnabled(name)) { return; }
        
        this.hook = hooker.apply(name).orElse(null);
        
        if (this.hook == null)
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

    @Override
    public Optional<String> economyName ()
    {
        if (this.hook == null)
            return Optional.empty();

        return this.hook.economyName();
    }
}
