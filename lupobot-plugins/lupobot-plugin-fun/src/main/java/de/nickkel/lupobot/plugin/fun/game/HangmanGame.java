package de.nickkel.lupobot.plugin.fun.game;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.fun.LupoFunPlugin;
import de.nickkel.lupobot.plugin.fun.enums.HangmanResult;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.awt.*;
import java.util.*;
import java.util.List;

public class HangmanGame {

    @Getter
    private final Member player;
    @Getter
    private final String word;
    @Getter @Setter
    private int chances = 0;
    @Getter
    private final int maxChances = 5;
    @Getter
    private final MultiValuedMap<String, String> characters = new ArrayListValuedHashMap<>();
    @Getter
    private final List<String> sortedCharacters = new ArrayList<>();
    @Getter
    private final List<String> incorrectCharacters = new ArrayList<>();
    @Getter
    private final CommandContext context;
    @Getter
    private Message message;
    @Getter @Setter
    private HangmanResult result;
    @Getter
    private String triedCharacter = "";
    @Getter
    private boolean active;
    @Getter
    private Timer timer;

    public HangmanGame(CommandContext context) {
        this.active = true;
        this.context = context;
        this.player = context.getMember();
        this.word = context.getPlugin().getLanguageHandler().getRandomTranslation(context.getServer().getLanguage(), "fun_hangman-word");
        for (int i = 0; i < this.word.length(); i++){
            this.characters.put(String.valueOf(this.word.charAt(i)), "_");
            this.sortedCharacters.add(String.valueOf(this.word.charAt(i)));
        }
        LupoFunPlugin.getInstance().getHangmanGames().add(this);
        build();
    }

    public void tryCharacter(String character) {
        if(this.characters.containsValue(character) || this.getIncorrectCharacters().contains(character)) {
            this.context.getChannel().sendMessage(this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-already-guessed", this.context.getMember().getAsMention())).queue();
            return;
        }
        this.timer.cancel();
        this.triedCharacter = character;
        boolean success = false;
        if(this.characters.containsKey(character) && !this.characters.containsValue(character)) {
            success = true;
        }

        if(success) {
            this.characters.get(character).clear();
            this.characters.get(character).add(character);
            this.result = HangmanResult.CORRECT;
        } else {
            this.incorrectCharacters.add(character);
            this.chances++;
            this.result = HangmanResult.FALSE;
        }

        if(!this.characters.containsValue("_")) {
            this.active = false;
            this.result = HangmanResult.WIN;
        }

        if(this.chances == this.maxChances) {
            this.active = false;
            this.result = HangmanResult.LOSE;
        }

        build();
    }

    public void build() {
        String currentWord = "";
        for(String character : this.sortedCharacters) {
            if(!this.characters.values().contains(character)) {
                currentWord = currentWord + "_ ";
            } else {
                currentWord = currentWord + character + " ";
            }
        }

        currentWord = "``" + currentWord + "``";

        String incorrectCharacters = "/";
        if(this.incorrectCharacters.size() != 0) {
            incorrectCharacters = "";
        }
        for(String character : this.incorrectCharacters) {
            incorrectCharacters = incorrectCharacters + character + ", ";
        }
        if(!incorrectCharacters.equals("/")) {
            incorrectCharacters = incorrectCharacters.substring(0, incorrectCharacters.length() - 2);
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.AQUA.getColor());
        String title = this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-write");
        if(this.result == HangmanResult.WIN) {
            title = this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-won-game");
        } else if(this.result == HangmanResult.LOSE) {
            title = this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-lost-game");
        } else if(this.result == HangmanResult.CORRECT) {
            title = this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-correct-letter", this.triedCharacter);
        } else if(this.result == HangmanResult.FALSE) {
            title = this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-false-letter", this.incorrectCharacters.get(this.incorrectCharacters.size()-1));
        }
        builder.setTitle(title);
        builder.setDescription(this.player.getAsMention());
        builder.setFooter(this.context.getServer().translate(context.getPlugin(), "fun_hangman-chances", this.chances, this.maxChances));
        builder.addField(this.context.getServer().translate(context.getPlugin(), "fun_hangman-incorrect-characters"), incorrectCharacters, false);
        builder.addField(this.context.getServer().translate(context.getPlugin(), "fun_hangman-current-word"), currentWord, false);

        if (this.message == null) {
            this.context.getChannel().sendMessage(builder.build()).queue(success -> {
                this.message = success;
            });
        } else {
            this.message.editMessage(builder.build()).queue();
        }

        if(this.result == HangmanResult.WIN) {
            LupoFunPlugin.getInstance().getHangmanGames().remove(this);
            this.context.getChannel().sendMessage(this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-won-message", this.getContext().getMember().getAsMention())).queue();
            return;
        } else if(this.result == HangmanResult.LOSE) {
            LupoFunPlugin.getInstance().getHangmanGames().remove(this);
            this.context.getChannel().sendMessage(this.context.getServer().translate(this.context.getPlugin(), "fun_hangman-lost-message", this.getContext().getMember().getAsMention(), this.getWord())).queue();
            return;
        }

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!LupoFunPlugin.getInstance().getHangmanGames().contains(HangmanGame.this)) {
                    this.cancel();
                    return;
                }

                HangmanGame.this.getContext().getChannel().sendMessage(HangmanGame.this.context.getServer().translate(HangmanGame.this.getContext().getPlugin(),
                        "fun_hangman-no-response", HangmanGame.this.context.getMember().getAsMention())).queue();
            }
        }, 60*1000L);
    }
}
