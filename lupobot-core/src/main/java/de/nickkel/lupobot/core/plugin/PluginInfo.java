package de.nickkel.lupobot.core.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PluginInfo {

    String name();

    String version() default "1.0.0";

    String author() default "Unknown";

    boolean autoEnabled() default false;

    boolean hidden() default  false;

}
