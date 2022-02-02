package me.justeli.coins.hook;

import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.api.economy.account.PlayerAccount;
import me.lokka30.treasury.api.economy.response.EconomyException;
import me.lokka30.treasury.api.economy.response.EconomySubscriber;
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionInitiator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

/** by Eli on February 01, 2022 **/
public class Economies
{
    private final JavaPlugin plugin;
    private final Set<String> missingPlugins = new HashSet<>();

    private final EconomyType economyType;
    private Economy vaultEconomy;
    private EconomyProvider treasuryEconomy;

    private Economies (JavaPlugin plugin)
    {
        this.plugin = plugin;

        for (EconomyType type : EconomyType.values())
        {
            if (isInstalled(type.pluginName()))
            {
                Optional<?> provider = getProvider(type.clazz(), type.pluginName());
                if (provider.isPresent())
                {
                    this.economyType = type;
                    switch (type)
                    {
                        case VAULT: this.vaultEconomy = (Economy) provider.get(); break;
                        case TREASURY: this.treasuryEconomy = (EconomyProvider) provider.get(); break;
                    }
                    this.missingPlugins.clear();
                    return;
                }
                else
                {
                    missingPlugins.add("an economy providing plugin for '" + type.pluginName() + "'");
                }
            }
        }

        if (missingPlugins.isEmpty())
        {
            this.missingPlugins.add(Arrays.stream(EconomyType.values()).map(EconomyType::pluginName).collect(Collectors.joining(" or ")));
        }

        this.economyType = null;
    }

    private boolean isInstalled (String name)
    {
        return plugin.getServer().getPluginManager().isPluginEnabled(name);
    }

    private <T> Optional<T> getProvider (Class<T> economyClass, String name)
    {
        try
        {
            RegisteredServiceProvider<T> rsp = plugin.getServer().getServicesManager().getRegistration(economyClass);
            if (rsp == null)
            {
                return Optional.empty();
            }

            this.plugin.getLogger().log(Level.INFO, name + " is used as the economy provider.");
            return Optional.of(rsp.getProvider());
        }
        catch (NullPointerException | NoClassDefFoundError throwable)
        {
            return Optional.empty();
        }
    }

    public static Economies of (JavaPlugin plugin)
    {
        return new Economies(plugin);
    }

    public Set<String> getMissingPluginNames ()
    {
        return this.missingPlugins;
    }

    public void balance (UUID uuid, Consumer<Double> balance)
    {
        switch (this.economyType)
        {
            case VAULT:
                balance.accept(vaultEconomy.getBalance(plugin.getServer().getOfflinePlayer(uuid)));
                break;
            case TREASURY:
                EconomyProvider economy = this.treasuryEconomy;
                economy.retrievePlayerAccount(uuid, new EconomySubscriber<PlayerAccount>()
                {
                    @Override
                    public void succeed (@NotNull PlayerAccount playerAccount)
                    {
                        playerAccount.retrieveBalance(economy.getPrimaryCurrency(), new EconomySubscriber<BigDecimal>()
                        {
                            @Override
                            public void succeed (@NotNull BigDecimal bigDecimal)
                            {
                                balance.accept(bigDecimal.doubleValue());
                            }

                            @Override
                            public void fail (@NotNull EconomyException exception) {}
                        });
                    }

                    @Override
                    public void fail (@NotNull EconomyException exception) {}
                });
                break;
        }
    }

    public void canAfford (UUID uuid, double amount, Consumer<Boolean> canAfford)
    {
        switch (this.economyType)
        {
            case VAULT:
                canAfford.accept(vaultEconomy.has(plugin.getServer().getOfflinePlayer(uuid), amount));
                break;
            case TREASURY:
                EconomyProvider economy = this.treasuryEconomy;
                economy.retrievePlayerAccount(uuid, new EconomySubscriber<PlayerAccount>()
                {
                    @Override
                    public void succeed (@NotNull PlayerAccount playerAccount)
                    {
                        playerAccount.canAfford(BigDecimal.valueOf(amount), economy.getPrimaryCurrency(), new EconomySubscriber<Boolean>()
                        {
                            @Override
                            public void succeed (@NotNull Boolean aBoolean)
                            {
                                canAfford.accept(aBoolean);
                            }

                            @Override
                            public void fail (@NotNull EconomyException exception) {}
                        });
                    }

                    @Override
                    public void fail (@NotNull EconomyException exception) {}
                });
                break;

        }
    }

    public void withdraw (UUID uuid, double amount, Runnable success)
    {
        switch (this.economyType)
        {
            case VAULT:
                if (vaultEconomy.withdrawPlayer(plugin.getServer().getOfflinePlayer(uuid), amount).transactionSuccess())
                {
                    success.run();
                }
                break;
            case TREASURY:
                EconomyProvider economy = this.treasuryEconomy;
                economy.retrievePlayerAccount(uuid, new EconomySubscriber<PlayerAccount>()
                {
                    @Override
                    public void succeed (@NotNull PlayerAccount playerAccount)
                    {
                        playerAccount.withdrawBalance(
                                BigDecimal.valueOf(amount),
                                EconomyTransactionInitiator.SERVER,
                                economy.getPrimaryCurrency(),
                                new EconomySubscriber<BigDecimal>()
                        {
                            @Override
                            public void succeed (@NotNull BigDecimal bigDecimal)
                            {
                                success.run();
                            }

                            @Override
                            public void fail (@NotNull EconomyException exception) {}
                        });
                    }

                    @Override
                    public void fail (@NotNull EconomyException exception) {}
                });
                break;
        }
    }

    public void deposit (UUID uuid, double amount, Runnable success)
    {
        switch (this.economyType)
        {
            case VAULT:
                if (vaultEconomy.depositPlayer(plugin.getServer().getOfflinePlayer(uuid), amount).transactionSuccess())
                {
                    success.run();
                }
                break;
            case TREASURY:
                EconomyProvider economy = this.treasuryEconomy;
                economy.retrievePlayerAccount(uuid, new EconomySubscriber<PlayerAccount>()
                {
                    @Override
                    public void succeed (@NotNull PlayerAccount playerAccount)
                    {
                        playerAccount.depositBalance(
                                BigDecimal.valueOf(amount),
                                EconomyTransactionInitiator.SERVER,
                                economy.getPrimaryCurrency(),
                                new EconomySubscriber<BigDecimal>()
                        {
                            @Override
                            public void succeed (@NotNull BigDecimal bigDecimal)
                            {
                                success.run();
                            }

                            @Override
                            public void fail (@NotNull EconomyException exception) {}
                        });
                    }

                    @Override
                    public void fail (@NotNull EconomyException exception) {}
                });
                break;
        }
    }
}
