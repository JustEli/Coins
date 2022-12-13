package me.justeli.coins.hook.treasury;

import com.google.gson.reflect.TypeToken;
import me.lokka30.treasury.api.economy.response.EconomyException;
import me.lokka30.treasury.api.economy.response.EconomySubscriber;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/* Rezz @ February 02, 2022 (creation) */
public final class EconomySubscribers
{
    private EconomySubscribers() {}

    @SuppressWarnings("unused")
    public static <T> SubscriberBuilder.PendingSuccess<T> requesting(Class<T> clazz)
    {
        return pendingSuccess();
    }

    @SuppressWarnings("unused")
    public static <T> SubscriberBuilder.PendingSuccess<T> requesting(TypeToken<T> type)
    {
        return pendingSuccess();
    }

    private static <T> SubscriberBuilder.PendingSuccess<T> pendingSuccess()
    {
        return success ->
        {
            Objects.requireNonNull(success, "success");
            return pendingFailure(success);
        };
    }

    private static <T> SubscriberBuilder.PendingFailure<T> pendingFailure(Consumer<T> success)
    {
        return failure ->
        {
            Objects.requireNonNull(failure, "failure");

            return new EconomySubscriber<T>()
            {
                @Override
                public void succeed(@NotNull T t) { success.accept(t); }

                @Override
                public void fail(@NotNull EconomyException exception) { failure.accept(exception); }
            };
        };
    }

    public interface SubscriberBuilder<T>
    {
        @FunctionalInterface
        interface PendingSuccess<T> extends SubscriberBuilder<T>
        {
            PendingFailure<T> success(Consumer<T> success);
        }

        @FunctionalInterface
        interface PendingFailure<T> extends SubscriberBuilder<T>
        {
            EconomySubscriber<T> failure(Consumer<EconomyException> failure);
            
            default EconomySubscriber<T> silentFailure() { return failure(ex -> {}); }
        }
    }
}
