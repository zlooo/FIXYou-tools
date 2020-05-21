package io.github.zlooo.performance.tester;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MessageExchange<T> {

    void sendMessage(@Nonnull T message);

    @Nullable
    T getSingleMessage();

    default void endOfBatch() {
    }
}
