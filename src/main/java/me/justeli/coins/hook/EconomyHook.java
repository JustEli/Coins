package me.justeli.coins.hook;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/** by Rezz on February 02, 2022 **/
public interface EconomyHook
{
    void balance (UUID uuid, DoubleConsumer balance);
    
    void canAfford (UUID uuid, double amount, Consumer<Boolean> canAfford);
    
    void withdraw (UUID uuid, double amount, Runnable success);
    
    void deposit (UUID uuid, double amount, Runnable success);

    Optional<String> economyName ();
}
