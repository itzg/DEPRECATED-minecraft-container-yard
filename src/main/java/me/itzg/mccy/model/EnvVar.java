package me.itzg.mccy.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to tag a setter method that accept a String or String[] of an environment variable.
 * @author Geoff Bourne
 * @since 3/14/2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnvVar {
    /**
     * @return the name of the environment variable
     */
    String value();
}
