package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "github", category = "general")
public class GithubCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(LupoColor.BLUE.getColor())
                .setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl())
                .setTimestamp(context.getTime())
                .setDescription(context.getServer().translate(context.getPlugin(), "help_github-message"))
                .addField(context.getServer().translate(context.getPlugin(), "help_github-link"),
                        LupoBot.getInstance().getConfig().getString("githubUrl"), false);
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
