package de.nickkel.lupobot.plugin.fun.commands;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.ThrowingBiConsumer;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.pagination.Page;
import de.nickkel.lupobot.core.pagination.Paginator;
import de.nickkel.lupobot.plugin.fun.LupoFunPlugin;
import de.nickkel.lupobot.plugin.fun.game.TicTacToeGame;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@CommandInfo(name = "tictactoe", aliases = "ttt", cooldown = 5, category = "game")
public class TicTacToeCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1) {
            Member member = context.getServer().getMember(context.getArgs()[0]);
            if (member == null) {
                sendSyntaxError(context, "fun_tictactoe-invalid-player");
                return;
            }

            if (member.getUser().isBot()) {
                sendSyntaxError(context, "fun_tictactoe-bot-player");
                return;
            }

            if (member.getIdLong() == context.getMember().getIdLong()) {
                sendSyntaxError(context, "fun_tictactoe-player-against-himself");
                return;
            }

            TicTacToeGame game = new TicTacToeGame(context);

            BiConsumer<Member, Message> accept = (reactor, message) -> {
                if (member == reactor && LupoFunPlugin.getInstance().getTicTacToeGames().contains(game) && game.getParticipant() == null) {
                    game.start(reactor);
                }
            };

            BiConsumer<Member, Message> deny = (reactor, message) -> {
                if (member == reactor && LupoFunPlugin.getInstance().getTicTacToeGames().contains(game) && game.getParticipant() == null) {
                    LupoFunPlugin.getInstance().getTicTacToeGames().remove(game);
                    context.getChannel().sendMessage(context.getServer().translate(context.getPlugin(), "fun_tictactoe-request-deny",
                            context.getMember().getAsMention(), reactor.getAsMention())).queue();
                }
            };

            List<Page> pages = new ArrayList<>();
            pages.add(new Page(Button.success("/", context.getServer().translate(context.getPlugin(), "fun_tictactoe-accept")), accept));
            pages.add(new Page(Button.danger("/", context.getServer().translate(context.getPlugin(), "fun_tictactoe-deny")), deny));

            context.getChannel().sendMessage(context.getServer().translate(context.getPlugin(), "fun_tictactoe-request", member.getAsMention(), context.getMember().getAsMention())).queue(success -> {
                Paginator.categorize(success, pages, 60);
            });
        } else {
            sendSyntaxError(context, "fun_tictactoe-no-player");
        }
    }
}
