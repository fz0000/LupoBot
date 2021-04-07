package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

@CommandInfo(name = "ticketlimit", category = "config", permissions = Permission.ADMINISTRATOR)
public class TicketLimitCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {

        if(context.getArgs().length == 1) {
            int amount;
            try {
                amount = Integer.parseInt(context.getArgs()[0]);
                context.getServer().appendPluginData(context.getPlugin(), "limitAmount", amount);
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "ticket_ticketlimit-invalid-amount");
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketlimit-info"));
            builder.addField(context.getServer().translate(context.getPlugin(), "ticket_ticketlimit-amount"),
                    String.valueOf(amount), false);
            builder.setTimestamp(context.getMessage().getTimeCreated());
            builder.setColor(LupoColor.GREEN.getColor());
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendHelp(context);
        }
    }
}
