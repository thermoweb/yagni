package fr.yagni.core.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Builder {

    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
    @interface Nullable {

    }
}
