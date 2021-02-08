package de.nickkel.lupobot.plugin.music;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;

@PluginInfo(name = "music", version = "1.0.0", author = "Nickkel")
public class LupoMusicPlugin extends LupoPlugin {

    @Override
    public void onEnable() {
        //LupoBot.getInstance().getCommandHandler().registerCommands(this, "de.nickkel.lupobot.plugin.music.commands");
    }

    @Override
    public void onDisable() {

    }
}
