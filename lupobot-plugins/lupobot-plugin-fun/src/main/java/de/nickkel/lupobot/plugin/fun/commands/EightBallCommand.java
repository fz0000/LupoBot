package de.nickkel.lupobot.plugin.fun.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandInfo(name = "eightball", aliases = "8ball", category = "general")
public class EightBallCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length >= 1) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.AQUA.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag(), null, context.getMember().getUser().getAvatarUrl());
            builder.addField(context.getServer().translate(context.getPlugin() ,"fun_eightball-question"), context.getArgsAsString(), false);
            builder.addField(context.getServer().translate(context.getPlugin() ,"fun_eightball-answer"),
                    context.getPlugin().getLanguageHandler().getRandomTranslation(context.getServer().getLanguage(), "fun_eightball-answer"), false);
            builder.setFooter(context.getServer().translate(context.getPlugin(), "fun_eightball-footer"));
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendSyntaxError(context, "fun_eightball-no-input");
        }

    }
}