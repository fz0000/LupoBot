package de.nickkel.lupobot.plugin.issuetracker.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.plugin.issuetracker.LupoIssueTrackerPlugin;
import de.nickkel.lupobot.plugin.issuetracker.entities.Issue;
import de.nickkel.lupobot.plugin.issuetracker.entities.IssueServer;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Arrays;
import java.util.List;

@CommandInfo(name = "addnote", category = "judger")
@SlashOption(name = "note", type = OptionType.STRING) @SlashOption(name = "id", type = OptionType.INTEGER)
public class AddNoteCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getSlash() == null && context.getArgs().length < 2) {
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
            String note = "";
            if (context.getSlash() == null) {
                List<String> args = Arrays.asList(context.getArgs());
                if (args.size() == 1) {
                    sendSyntaxError(context, "issuetracker_commands-no-message");
                    return;
                }
                args = args.subList(1, args.size());

                for (String arg : args) {
                    note += arg + " ";
                }
            } else {
                note = context.getSlash().getOption("message").getAsString();
            }

            issue.note(context.getMember(), note);
        } else {
            sendSyntaxError(context, "issuetracker_commands-invalid-issue");
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
