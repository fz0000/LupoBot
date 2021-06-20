package de.nickkel.lupobot.plugin.fun.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "penissize", aliases = "ps", category = "general")
@SlashOption(name = "member", type = OptionType.USER, required = false)
public class PenisSizeCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1 || (context.getSlash() != null && context.getSlash().getOption("member") != null)) {
            if (context.getSlash() == null) {
                send(context, getEmbed(context, context.getServer().getMember(context.getArgs()[0])));
            } else {
                send(context, getEmbed(context, context.getSlash().getOption("member").getAsMember()));
            }
        } else {
            send(context, (getEmbed(context, context.getMember()).build()));
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
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
