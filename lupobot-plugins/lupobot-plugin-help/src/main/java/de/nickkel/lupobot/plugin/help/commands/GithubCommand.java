package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.LocalDateTime;

@CommandInfo(name = "github", category = "general")
public class GithubCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#0066CC"));
        builder.setAuthor(LupoBot.getInstance().getJda().getSelfUser().getName(), null, LupoBot.getInstance().getJda().getSelfUser().getAvatarUrl());
        builder.setTimestamp(LocalDateTime.now());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "help_github-message"));
        builder.addField(context.getServer().translate(context.getPlugin(), "help_github-link"),
                LupoBot.getInstance().getConfig().getString("githubUrl"), false);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
