package stacker.flow;

import stacker.common.IParser;

/**
 * @param <A> ArgumentType
 * @param <R> ReturnType
 */

public final class Contract<A, R> {
    private final Class<A> argumentClass;
    private final Class<R> returnClass;
    private final IParser parser;

    public Contract(Class<A> argumentClass, Class<R> returnClass, IParser parser) {
        this.argumentClass = argumentClass;
        this.returnClass = returnClass;
        this.parser = parser;
    }

    public Class<A> getArgumentClass() {
        return argumentClass;
    }

    public Class<R> getReturnClass() {
        return returnClass;
    }

    public IParser getParser() {
        return parser;
    }

}
