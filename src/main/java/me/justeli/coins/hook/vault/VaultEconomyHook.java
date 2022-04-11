package me.justeli.coins.hook.vault;

import me.justeli.coins.hook.EconomyHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/** by Rezz on February 02, 2022 **/
public final class VaultEconomyHook implements EconomyHook
{
    public static final String VAULT = "Vault";
    
    private final Plugin plugin;
    private final Economy economy;
    
    public VaultEconomyHook (Plugin plugin, Economy economy)
    {
        this.plugin = plugin;
        this.economy = economy;
    }
    
    @Override
    public void balance (UUID uuid, DoubleConsumer balance)
    {
        balance.accept(economy.getBalance(plugin.getServer().getOfflinePlayer(uuid)));
    }
    
    @Override
    public void canAfford (UUID uuid, double amount, Consumer<Boolean> canAfford)
    {
        canAfford.accept(economy.has(plugin.getServer().getOfflinePlayer(uuid), amount));
    }
    
    @Override
    public void withdraw (UUID uuid, double amount, Runnable success)
    {
        if (economy.withdrawPlayer(plugin.getServer().getOfflinePlayer(uuid), amount).transactionSuccess())
        {
            success.run();
        }
    }
    
    @Override
    public void deposit (UUID uuid, double amount, Runnable success)
    {
        if (economy.depositPlayer(plugin.getServer().getOfflinePlayer(uuid), amount).transactionSuccess())
        {
            success.run();
        }
    }

    @Override
    public Optional<String> name ()
    {
        return Optional.of(VAULT);
    }
}
