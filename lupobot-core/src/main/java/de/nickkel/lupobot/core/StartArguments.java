package de.nickkel.lupobot.core;

public class StartArguments {

    public static final boolean MAINTENANCE = LupoBot.getInstance().getCommandLineArgs().contains("--maintenance");

}