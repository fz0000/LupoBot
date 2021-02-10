package de.nickkel.lupobot.plugin.fun.game;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.fun.LupoFunPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class TicTacToeGame {

    @Getter
    private final Member creator;
    @Getter
    private Member participant, currentPlayer, winner;
    @Getter
    private final Map<Integer, String> field = new HashMap<>();
    @Getter
    private final CommandContext context;
    @Getter
    private Message message = null;
    @Getter
    private final List<int[]> bestMoves = new ArrayList<>();
    @Getter
    private Timer timer;

    public TicTacToeGame(CommandContext context) {
        this.creator = context.getMember();
        this.context = context;
        this.bestMoves.add(new int[]{1, 2, 3});
        this.bestMoves.add(new int[]{4, 5, 6});
        this.bestMoves.add(new int[]{7, 8, 9});
        this.bestMoves.add(new int[]{1, 5, 9});
        this.bestMoves.add(new int[]{3, 5, 7});
        for(int i = 1; i < 10; i++) {
            field.put(i, getEmoji(i));
        }
        LupoFunPlugin.getInstance().getTicTacToeGames().add(this);
    }

    public void start(Member participant) {
        this.participant = participant;
        this.currentPlayer = creator;
        build(false);
    }

    public void build(boolean end) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.AQUA.getColor());
            builder.setTimestamp(this.context.getMessage().getTimeCreated());
            builder.setFooter(this.context.getServer().translate(this.context.getPlugin(), "fun_tictactoe-footer", creator.getUser().getAsTag()));
            builder.setTitle(this.context.getServer().translate(this.context.getPlugin(), "fun_tictactoe-title"));

            String field = "";
            List<Integer> backslashes = Arrays.asList(3, 6);
            for (int i = 1; i < 10; i++) {
                String addition = "";
                if (backslashes.contains(i)) {
                    addition = "\n";
                }
                field = field + this.field.get(i) + addition;
            }
            builder.setDescription(field);
            if(end) {
                builder.addField(this.context.getServer().translate(this.context.getPlugin(), "fun_tictactoe-winner"), this.winner.getAsMention(), false);
            } else {
                builder.addField(this.context.getServer().translate(this.context.getPlugin(), "fun_tictactoe-current-player"), this.currentPlayer.getAsMention(), false);
            }

            if(end) {
                this.message.editMessage(builder.build()).queue();
                return;
            }

            Map<Integer, BiConsumer<Member, Message>> consumers = new HashMap<>();
            for (int i = 1; i < 10; i++) {
                int finalI = i;
                BiConsumer<Member, Message> consumer = (member, message) -> {
                    if (this.getCurrentPlayer().getIdLong() == member.getIdLong()) {
                        String emoji = ":o:";
                        if (this.getCreator() == this.getCurrentPlayer()) {
                            emoji = ":x:";
                        }
                        this.field.replace(finalI, emoji);
                        nextRound();
                    }
                };
                consumers.put(i, consumer);
            }

            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!LupoFunPlugin.getInstance().getTicTacToeGames().contains(TicTacToeGame.this)) {
                        this.cancel();
                        return;
                    }
                    Member winner = TicTacToeGame.this.getCreator();
                    if(winner == TicTacToeGame.this.getCurrentPlayer()) {
                        winner = TicTacToeGame.this.getParticipant();
                    }
                    TicTacToeGame.this.getContext().getChannel().sendMessage(TicTacToeGame.this.context.getServer().translate(TicTacToeGame.this.getContext().getPlugin(),
                            "fun_tictactoe-no-response", TicTacToeGame.this.currentPlayer.getAsMention())).queue();
                    end(winner);
                }
            }, 60*1000L);


            if (this.message == null) {
                this.context.getChannel().sendMessage(builder.build()).queue(success -> {
                    this.message = success;
                    for (int i = 1; i < 10; i++) {
                        if(!this.field.get(i).equals(":x:") && !this.field.get(i).equals(":o:")) {
                            Pages.buttonize(success, Collections.singletonMap(getEmoji(i), consumers.get(i)), false, 60, TimeUnit.SECONDS);
                        }
                    }
                });
            } else {
                for (int i = 1; i < 10; i++) {
                    if(this.field.get(i).equals(":x:") || this.field.get(i).equals(":o:")) {
                        this.message.removeReaction(getEmoji(i)).queue();
                    }
                }
                this.message.editMessage(builder.build()).queue(success -> {
                    for (int i = 1; i < 10; i++) {
                        if(!this.field.get(i).equals(":x:") && !this.field.get(i).equals(":o:")) {
                            Pages.buttonize(success, Collections.singletonMap(getEmoji(i), consumers.get(i)), false, 60, TimeUnit.SECONDS);
                        }
                    }
                });
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void nextRound() {
        try {
            this.timer.cancel();
            String emoji = "";
            if(this.getCreator() == this.getCurrentPlayer()) {
                this.currentPlayer = this.getParticipant();
                emoji = ":x:";
            } else {
                this.currentPlayer = this.getCreator();
                emoji = ":o:";
            }

            Member winner = null;

            for (int[] move : bestMoves) {
                int score = 0;
                for (int number = 1; number < this.field.size(); number++) {
                    if (this.field.get(number).equals(emoji) && (move[0] == number || move[1] == number || move[2] == number)) {
                        score++;
                    }
                }

                if (score == 3) {
                    if (emoji.equals(":o:")) {
                        winner = this.getParticipant();
                    } else {
                        winner = this.getCreator();
                    }
                }
            }

            int checked = 0;
            for(int i = 1; i < 10; i++) {
                if(this.field.get(i).equals(":x:") || this.field.get(i).equals(":o:")) {
                    checked++;
                }
            }

            if(checked == 9) {
                end(null);
                return;
            }

            if(winner == null) {
                build(false);
            } else {
                this.winner = winner;
                build(true);
                end(winner);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void end(Member winner) {
        LupoFunPlugin.getInstance().getTicTacToeGames().remove(this);
        for (int i = 1; i < 10; i++) {
            this.message.removeReaction(getEmoji(i)).queue();
        }

        if(winner == null) {
            this.context.getChannel().sendMessage(this.context.getServer().translate(context.getPlugin(), "fun_tictactoe-draw")).queue();
        } else {
            this.context.getChannel().sendMessage(this.context.getServer().translate(context.getPlugin(), "fun_tictactoe-end", winner.getAsMention())).queue();
        }

    }

    public Member getNextMember() {
        if(this.currentPlayer == this.participant) {
            return this.creator;
        } else {
            return this.participant;
        }
    }
    public String getEmoji(int number) {
        switch(number) {
            default:
                return "1️⃣";
            case 2:
                return "2️⃣";
            case 3:
                return "3️⃣";
            case 4:
                return "4️⃣";
            case 5:
                return "5️⃣";
            case 6:
                return "6️⃣";
            case 7:
                return "7️⃣";
            case 8:
                return "8️⃣";
            case 9:
                return "9️⃣";
        }
    }
}
