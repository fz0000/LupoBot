package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.LocalDateTime;

@CommandInfo(name = "github", category = "general")
public class GithubCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(LupoBot.getInstance().getJda().getSelfUser().getName(), null, LupoBot.getInstance().getJda().getSelfUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "help_github-message"));
        builder.addField(context.getServer().translate(context.getPlugin(), "help_github-link"),
                LupoBot.getInstance().getConfig().getString("githubUrl"), false);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
