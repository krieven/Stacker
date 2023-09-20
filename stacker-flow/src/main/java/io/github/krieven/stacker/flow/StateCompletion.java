package io.github.krieven.stacker.flow;


import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

public final class StateCompletion {

    private Runnable runnable = null;

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
