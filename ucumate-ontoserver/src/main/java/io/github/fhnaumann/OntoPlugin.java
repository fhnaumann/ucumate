package io.github.fhnaumann;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.Collection;

/**
 * @author Felix Naumann
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface OntoPlugin {

    String name();
    String[] systems();
}
