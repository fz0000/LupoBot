package de.nickkel.lupobot.plugin.help;

import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;

@PluginInfo(name = "help", author = "Nickkel")
public class LupoHelpPlugin extends LupoPlugin {

    @Override
    public void onEnable() {
        this.registerCommands("de.nickkel.lupobot.plugin.help.commands");
    }

    @Override
    public void onDisable() {

    }
}
