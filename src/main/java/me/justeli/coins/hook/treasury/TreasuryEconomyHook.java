package me.justeli.coins.hook.treasury;

import me.justeli.coins.hook.EconomyHook;
import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.api.economy.account.PlayerAccount;
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionInitiator;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/* Rezz @ February 02, 2022 (creation) */
public final class TreasuryEconomyHook
    implements EconomyHook
{
    public static final String TREASURY = "Treasury";

    private final EconomyProvider economy;

    public TreasuryEconomyHook (EconomyProvider economy)
    {
        this.economy = economy;
    }

    @Override
    public void balance (UUID uuid, DoubleConsumer balance)
    {
        economy.retrievePlayerAccount(
            uuid,
            EconomySubscribers.requesting(PlayerAccount.class)
                .success(playerAccount ->
                    playerAccount.retrieveBalance(
                        economy.getPrimaryCurrency(),
                        EconomySubscribers.requesting(BigDecimal.class)
                            .success(bigDecimal -> balance.accept(bigDecimal.doubleValue()))
                            .silentFailure()
                    )
                )
                .silentFailure()
        );
    }

    @Override
    public void canAfford (UUID uuid, double amount, Consumer<Boolean> canAfford)
    {
        economy.retrievePlayerAccount(
            uuid,
            EconomySubscribers.requesting(PlayerAccount.class)
                .success(playerAccount ->
                    playerAccount.canAfford(
                        BigDecimal.valueOf(amount),
                        economy.getPrimaryCurrency(),
                        EconomySubscribers.requesting(Boolean.class)
                            .success(canAfford)
                            .silentFailure()
                    )
                )
                .silentFailure()
        );
    }

    @Override
    public void withdraw (UUID uuid, double amount, Runnable success)
    {
         economy.retrievePlayerAccount(
            uuid,
            EconomySubscribers.requesting(PlayerAccount.class)
                .success(playerAccount ->
                    playerAccount.withdrawBalance(
                        BigDecimal.valueOf(amount),
                        EconomyTransactionInitiator.SERVER,
                        economy.getPrimaryCurrency(),
                        EconomySubscribers.requesting(BigDecimal.class)
                            .success(bigDecimal -> success.run())
                            .silentFailure()
                    )
                )
                .silentFailure()
        );
    }

    @Override
    public void deposit (UUID uuid, double amount, Runnable success)
    {
        economy.retrievePlayerAccount(
            uuid,
            EconomySubscribers.requesting(PlayerAccount.class)
                .success(playerAccount ->
                    playerAccount.depositBalance(
                        BigDecimal.valueOf(amount),
                        EconomyTransactionInitiator.SERVER,
                        economy.getPrimaryCurrency(),
                        EconomySubscribers.requesting(BigDecimal.class)
                            .success(bigDecimal -> success.run())
                            .silentFailure()
                    )
                )
                .silentFailure()
        );
    }

    @Override
    public Optional<String> name ()
    {
        return Optional.of(TREASURY);
    }
}
