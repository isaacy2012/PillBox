package com.innerCat.pillBox.util;


import androidx.core.util.Consumer;

public class IfNotNull {
    public static <T> void ifNotNull(T obj, Consumer<T> fun) {
        if (obj != null) {
            fun.accept(obj);
        }
    }
}
