package stacker.flow;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class StateCompletion {

    private final Runnable runnable;

    @ApiStatus.Experimental
    public StateCompletion(CompletableFuture<StateCompletion> future) {
        runnable = () -> {
        };
    }

    StateCompletion(@NotNull Runnable runnable) {
        this.runnable = runnable;
    }

    void completeState() {
        runnable.run();
    }

}
