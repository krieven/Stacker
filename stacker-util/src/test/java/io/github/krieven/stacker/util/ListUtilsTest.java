package io.github.krieven.stacker.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ListUtilsTest {
    @Test
    public void testAlisOf(){
        List<String> strings = ListUtils.aListOf("a", "b", "c");
        Assert.assertNotNull(strings);
        Assert.assertEquals(3, strings.size());
    }
}
