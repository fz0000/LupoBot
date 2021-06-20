package de.nickkel.lupobot.core.command;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SlashSubCommands.class)
public @interface SlashSubCommand {

    String name();

    SlashOption[] options() default {};
}
