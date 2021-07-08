package de.nickkel.lupobot.plugin.fun;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.core.util.ListenerRegister;
import de.nickkel.lupobot.plugin.fun.game.HangmanGame;
import de.nickkel.lupobot.plugin.fun.game.TicTacToeGame;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@PluginInfo(name = "fun", version = "1.0.0", author = "Nickkel")
public class LupoFunPlugin extends LupoPlugin {

    @Getter
    private static LupoFunPlugin instance;
    @Getter
    private final List<TicTacToeGame> ticTacToeGames = new ArrayList<>();
    @Getter
    private final List<HangmanGame> hangmanGames = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands("de.nickkel.lupobot.plugin.fun.commands");
        this.registerListeners("de.nickkel.lupobot.plugin.fun.listener");
    }

    @Override
    public void onDisable() {

    }
}
