package de.nickkel.lupobot.plugin.issuetracker.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.plugin.issuetracker.LupoIssueTrackerPlugin;
import de.nickkel.lupobot.plugin.issuetracker.entities.Issue;
import de.nickkel.lupobot.plugin.issuetracker.entities.IssueServer;
import de.nickkel.lupobot.plugin.issuetracker.enums.IssuePriority;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Arrays;
import java.util.List;

@CommandInfo(name = "masterapprove", category = "admin")
@SlashOption(name = "message", type = OptionType.STRING) @SlashOption(name = "id", type = OptionType.INTEGER)
@SlashOption(name = "priority", type = OptionType.STRING, choices = {"LOW", "MEDIUM", "HIGH"})
public class MasterApproveCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getSlash() == null && context.getArgs().length < 3) {
            sendHelp(context);
            return;
        }

        int id;
        if (context.getSlash() != null) {
            id = Integer.parseInt(context.getSlash().getOption("id").getAsString());
        } else {
            try {
                id = Integer.parseInt(context.getArgs()[0]);
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "issuetracker_commands-invalid-id");
                return;
            }
        }

        IssueServer server = LupoIssueTrackerPlugin.getInstance().getIssueServer(context.getGuild());
        Issue issue = server.getIssue(id);
        if (issue != null) {
            IssuePriority priority;
            try {
                if (context.getSlash() == null) {
                    priority = IssuePriority.valueOf(context.getArgs()[1].toUpperCase());
                } else {
                    priority = IssuePriority.valueOf(context.getSlash().getOption("priority").getAsString());
                }
            } catch (Exception e) {
                sendSyntaxError(context, "issuetracker_commands-invalid-priority");
                return;
            }

            String message = "";
            if (context.getSlash() == null) {
                List<String> args = Arrays.asList(context.getArgs());
                if (args.size() == 2) {
                    sendSyntaxError(context, "issuetracker_commands-no-message");
                    return;
                }
                args = args.subList(2, args.size());

                for (String arg : args) {
                    message += arg + " ";
                }
            } else {
                message = context.getSlash().getOption("message").getAsString();
            }

            issue.approve(context.getMember(), message, priority, true);
        } else {
            sendSyntaxError(context, "issuetracker_commands-invalid-issue");
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
