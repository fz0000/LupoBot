package de.nickkel.lupobot.plugin.fun.listener;

import de.nickkel.lupobot.plugin.fun.LupoFunPlugin;
import de.nickkel.lupobot.plugin.fun.enums.HangmanResult;
import de.nickkel.lupobot.plugin.fun.game.HangmanGame;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HangmanListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        HangmanGame game = null;
        for (HangmanGame all : LupoFunPlugin.getInstance().getHangmanGames()) {
            if (all.getPlayer() == event.getMember()) {
                game = all;
            }
        }
        if (game == null || game.getMessage() == null) {
            return;
        }

        if (event.getChannel() == game.getMessage().getTextChannel() && event.getMember() == game.getContext().getMember() && game.isActive()) {
            if (event.getMessage().getContentRaw().length() == 1 && event.getMessage().getContentRaw().chars().allMatch(Character::isLetter)) {
                game.tryCharacter(event.getMessage().getContentRaw());
            } else {
                if (event.getMessage().getContentRaw().equalsIgnoreCase(game.getWord())) {
                    for (String key : game.getSortedCharacters()) {
                        game.getCharacters().get(key).clear();
                        game.getCharacters().get(key).add(key);
                    }
                    game.setResult(HangmanResult.WIN);
                    game.build();
                } else {
                    if(event.getMessage().getContentRaw().length() != 1) {
                        event.getChannel().sendMessage(game.getContext().getServer().translate(game.getContext().getPlugin(), "fun_hangman-word-length",
                                event.getMember().getAsMention(), game.getWord().length())).queue();
                    } else {
                        event.getChannel().sendMessage(game.getContext().getServer().translate(game.getContext().getPlugin(), "fun_hangman-only-letters",
                                event.getMember().getAsMention())).queue();
                    }
                }
            }
        }
        event.getMessage().delete().queue();
    }
}
