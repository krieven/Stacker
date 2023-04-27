package io.github.krieven.stacker.util;

import org.junit.Assert;
import org.junit.Test;


public class TriableTest {

    @Test
    public void testTryOk() {
        Assert.assertEquals("Hello", Triable.tryGet(() -> "Hello").orElse(null));
    }

    @Test
    public void testTryBad() {
        Assert.assertEquals("World", Triable.tryGet(() -> {
            throw new RuntimeException();
        }).orElse("World"));
    }
}
