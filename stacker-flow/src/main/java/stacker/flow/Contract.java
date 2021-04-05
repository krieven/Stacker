package stacker.flow;

import stacker.common.IParser;

public final class Contract<A, R> {
    private Class<A> argumentClass;
    private Class<R> returnClass;
    private IParser parser;

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
