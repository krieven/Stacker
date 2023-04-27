package io.github.krieven.stacker.util;

import org.junit.Assert;
import org.junit.Test;


public class ProbeTest {

    @Test
    public void testTryOk() {
        Assert.assertEquals("Hello", Probe.tryGet(() -> "Hello").orElse(null));
    }

    @Test
    public void testTryBad() {
        Assert.assertEquals("World", Probe.tryGet(() -> {
            throw new RuntimeException();
        }).orElse("World"));
    }
}
