package de.nickkel.lupobot.plugin.issuetracker.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.plugin.issuetracker.LupoIssueTrackerPlugin;
import de.nickkel.lupobot.plugin.issuetracker.entities.IssueCreator;
import de.nickkel.lupobot.plugin.issuetracker.entities.IssueServer;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "createissue", aliases = "issue", category = "user")
public class CreateIssueCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        IssueServer server = LupoIssueTrackerPlugin.getInstance().getIssueServer(context.getGuild());
        if (server.getCreators().containsKey(context.getMember().getIdLong())) {
            sendSyntaxError(context, "issuetracker_createissue-already-creation");
            return;
        }
        if (context.getChannel().getIdLong() != server.getCreationChannel().getIdLong()) {
            sendSyntaxError(context, "issuetracker_createissue-incorrect-channel", server.getCreationChannel().getAsMention());
            return;
        }

        new IssueCreator(context.getChannel(), context.getMember());
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
