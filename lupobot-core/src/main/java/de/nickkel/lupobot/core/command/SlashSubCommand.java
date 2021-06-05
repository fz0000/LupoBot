package de.nickkel.lupobot.core.command;

public @interface SlashSubCommand {

    String name();

    SlashOption[] options();
}
