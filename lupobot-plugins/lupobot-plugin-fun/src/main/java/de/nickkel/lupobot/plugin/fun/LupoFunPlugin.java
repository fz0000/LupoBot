package de.nickkel.lupobot.plugin.fun;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;

@PluginInfo(name = "fun", version = "1.0.0", author = "Nickkel")

public class LupoFunPlugin extends LupoPlugin {
    @Override
    public void onEnable() {
        LupoBot.getInstance().getCommandHandler().registerCommands(this, "de.nickkel.lupobot.plugin.fun.commands");
    }

    @Override
    public void onDisable() {

    }
}
