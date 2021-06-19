package de.nickkel.lupobot.plugin.fun.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "eightball", aliases = "8ball", category = "general")
@SlashOption(name = "question", type = OptionType.STRING)
public class EightBallCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length >= 1 || context.getSlash() != null) {
            String args = context.getArgsAsString();
            if (context.getSlash() != null) {
                args = context.getSlash().getOption("question").getAsString();
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.AQUA.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.addField(context.getServer().translate(context.getPlugin() ,"fun_eightball-question"), args, false);
            builder.addField(context.getServer().translate(context.getPlugin() ,"fun_eightball-answer"),
                    context.getPlugin().getLanguageHandler().getRandomTranslation(context.getServer().getLanguage(), "fun_eightball-answer"), false);
            builder.setFooter(context.getServer().translate(context.getPlugin(), "fun_eightball-footer"));
            send(context, builder);
        } else {
            sendSyntaxError(context, "fun_eightball-no-input");
        }

    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {

    }
}