package de.nickkel.lupobot.plugin.fun.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

@CommandInfo(name = "penissize", aliases = "ps", category = "general")
public class PenisSizeCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1) {
            context.getChannel().sendMessage(getEmbed(context, context.getServer().getMember(context.getArgs()[0])).build()).queue();
        } else {
            context.getChannel().sendMessage(getEmbed(context, context.getMember()).build()).queue();
        }
    }

    private EmbedBuilder getEmbed(CommandContext context, Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        if (member.getUser().getIdLong() == LupoBot.getInstance().getSelfUser().getIdLong()) {
            builder.setDescription(context.getServer().translate(context.getPlugin(), "fun_penissize-lupo", context.getMember()));
        } else {
            builder.setDescription(context.getPlugin().getLanguageHandler().getRandomTranslation(context.getServer().getLanguage(),
                    "fun_penissize-message", member.getAsMention()));
        }
        builder.setColor(LupoColor.AQUA.getColor());
        builder.setFooter(context.getServer().translate(context.getPlugin(), "fun_penissize-footer"));
        return builder;
    }
}
