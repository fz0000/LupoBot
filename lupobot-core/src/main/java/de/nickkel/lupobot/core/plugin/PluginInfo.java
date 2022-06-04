package de.nickkel.lupobot.core.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PluginInfo {

    String name();

    String author() default "N/A";

    String version() default "1.0.0";

    boolean hidden() default false;

}
