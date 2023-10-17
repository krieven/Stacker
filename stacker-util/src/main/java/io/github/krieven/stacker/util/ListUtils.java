package io.github.krieven.stacker.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListUtils {

    public static <T> List<T> aListOf(T... obj) {
        return Arrays.stream(obj).collect(Collectors.toList());
    }
}
