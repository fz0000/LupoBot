package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.ticket.enums.TicketState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "ticketcategory", category = "config", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "state", type = OptionType.STRING, choices = {"OPENED", "CLOSED", "CLAIMED"})
@SlashOption(name = "category", type = OptionType.CHANNEL, required = false)
public class TicketCategoryCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 1 || context.getSlash() != null) {
            String states = "";
            for (TicketState state : TicketState.values()) {
                states = states + state.toString() + ", ";
            }
            states = states.substring(0, states.length() - 2);

            TicketState ticketState;
            try {
                if (context.getSlash() == null) {
                    ticketState = TicketState.valueOf(args[0].toUpperCase());
                } else {
                    ticketState = TicketState.valueOf(context.getSlash().getOption("state").getAsString());
                }
            } catch (IllegalArgumentException e) {
                sendSyntaxError(context, "ticket_ticketcategory-invalid-state", states);
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            Category category = context.getChannel().getParent();
            if (context.getSlash() != null && context.getSlash().getOption("category") != null &&
                    context.getSlash().getOption("category").getAsGuildChannel() instanceof Category) {
                category = (Category) context.getSlash().getOption("category").getAsGuildChannel();
            }
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
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.GREEN.getColor());
            send(context, builder);
        } else {
            sendHelp(context);
        }

    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
