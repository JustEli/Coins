package me.justeli.coins.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Created by Eli on July 09, 2021. */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public @interface ConfigEntry
{
    String value ();

    boolean required () default true;

    String motivation () default "";
}
