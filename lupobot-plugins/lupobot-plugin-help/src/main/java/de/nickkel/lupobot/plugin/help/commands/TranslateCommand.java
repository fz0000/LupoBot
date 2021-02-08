package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.LocalDateTime;

@CommandInfo(name = "translate", aliases = "crowdin", category = "general")
public class TranslateCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.YELLOW.getColor());
        builder.setAuthor(LupoBot.getInstance().getJda().getSelfUser().getName(), null, LupoBot.getInstance().getJda().getSelfUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "help_translate-message"));
        builder.addField(context.getServer().translate(context.getPlugin(), "help_translate-link"),
                LupoBot.getInstance().getConfig().getString("translateUrl"), false);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
