package de.nickkel.lupobot.core.command;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SlashOption {

    String name();

    OptionType type();

    String[] choices() default {};

    boolean required() default true;
}
