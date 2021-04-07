package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.ticket.enums.TicketState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;

@CommandInfo(name = "ticketcategory", category = "config", permissions = Permission.ADMINISTRATOR)
public class TicketCategoryCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 1) {
            String states = "";
            for (TicketState state : TicketState.values()) {
                states = states + state.toString() + ", ";
            }
            states = states.substring(0, states.length() - 2);

            TicketState ticketState = null;
            try {
                ticketState = TicketState.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                sendSyntaxError(context, "ticket_ticketcategory-invalid-state", states);
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            Category category = context.getChannel().getParent();
            if (category == null) {
                context.getServer().appendPluginData(context.getPlugin(), ticketState.getKey(), -1);
                builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketcategory-none"));
            } else {
                context.getServer().appendPluginData(context.getPlugin(), ticketState.getKey(), category.getIdLong());
                builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketcategory-success"));
                builder.addField(context.getServer().translate(context.getPlugin(), "ticket_ticketcategory-category"),
                        category.getName() + " (" + category.getId() + ")", false);
            }

            builder.addField(context.getServer().translate(context.getPlugin(), "ticket_ticketcategory-state"),
                    ticketState.toString(), false);
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setTimestamp(context.getMessage().getTimeCreated());
            builder.setColor(LupoColor.GREEN.getColor());
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendHelp(context);
        }

    }
}
