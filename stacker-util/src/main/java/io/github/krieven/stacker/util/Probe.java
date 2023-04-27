package io.github.krieven.stacker.util;

import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

/**
 * Super magic getter
 * @param <R> type of returning value
 */
public class Probe<R> {

    private final Supplier<R> supplier;

    private Probe(Supplier<R> supplier) {
        this.supplier = supplier;
    }

    /**
     * Creates new Triable with Supplier
     * @param supplier try block Supplier of R
     * @return Triable of R
     * @param <R> Type of returning value
     */
    public static <R> Probe<R> tryGet(@NotNull Supplier<R> supplier) {
        return new Probe<>(supplier);
    }

    /**
     * If Supplier throws any Throwable then return default value,
     * otherwise return result of Supplier
     * @param value default value
     * @return If Supplier throws any Throwable then return default value,
     * otherwise return result of Supplier
     */
    public R orElse(R value){
        try {
            return supplier.get();
        } catch (Throwable any) {
            return value;
        }
    }
}
