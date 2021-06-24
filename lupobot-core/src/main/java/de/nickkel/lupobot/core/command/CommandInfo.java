package de.nickkel.lupobot.core.command;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    String name();

    String category();

    String[] aliases() default {};

    Permission[] permissions() default {};

    int cooldown() default 0;

    int staffPower() default -1;

    boolean hidden() default false;
}
