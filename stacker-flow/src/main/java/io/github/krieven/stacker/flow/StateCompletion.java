package io.github.krieven.stacker.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class StateCompletion {

    private Runnable runnable = null;

    @ApiStatus.Experimental
    public StateCompletion(CompletableFuture<StateCompletion> future) {
        future.thenAccept(StateCompletion::doCompletion);
    }

    StateCompletion(@NotNull Runnable runnable) {
        this.runnable = runnable;
    }

    void doCompletion() {
        if (runnable == null) {
            return;
        }
        runnable.run();
    }

}
