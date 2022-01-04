package de.nickkel.lupobot.core.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PluginInfo {

    String name();

    String author() default "Unknown";

    String version() default "1.0.0-SNAPSHOT";

    long[] guildWhitelist() default {};

    boolean hidden() default false;

}
