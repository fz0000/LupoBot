package de.nickkel.lupobot.plugin.help;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;

@PluginInfo(name = "help", version = "1.0.0", author = "Nickkel")
public class LupoHelpPlugin extends LupoPlugin {

    @Override
    public void onEnable() {
        LupoBot.getInstance().getCommandHandler().registerCommands(this, "de.nickkel.lupobot.plugin.help.commands");
    }

    @Override
    public void onDisable() {

    }
}
