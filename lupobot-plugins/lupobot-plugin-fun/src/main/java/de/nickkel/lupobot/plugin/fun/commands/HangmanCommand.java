package de.nickkel.lupobot.plugin.fun.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.plugin.fun.game.HangmanGame;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "hangman", cooldown = 5, category = "game")
public class HangmanCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        new HangmanGame(context);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}