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
import org.apache.commons.validator.routines.UrlValidator;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(name = "addattachment", aliases = "addattach", category = "judger")
@SlashOption(name = "link", type = OptionType.STRING) @SlashOption(name = "id", type = OptionType.INTEGER)
public class AddAttachmentCommand extends LupoCommand {

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
            String link = "";
            if (context.getSlash() == null) {
                if (context.getArgs().length == 1) {
                    sendSyntaxError(context, "issuetracker_addattachment-no-attachment");
                    return;
                }
                link = context.getArgs()[1];
            } else {
                link = context.getSlash().getOption("message").getAsString();
            }

            if (!new UrlValidator().isValid(link)) {
                sendSyntaxError(context, "issuetracker_addattachment-no-valid-url");
                return;
            }
            issue.attach(context.getMember(), link);
        } else {
            sendSyntaxError(context, "issuetracker_commands-invalid-issue");
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
