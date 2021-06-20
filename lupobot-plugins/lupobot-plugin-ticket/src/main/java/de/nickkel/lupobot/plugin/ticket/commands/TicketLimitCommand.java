package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "ticketlimit", category = "config", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "limit", type = OptionType.INTEGER)
public class TicketLimitCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1 || context.getSlash() != null) {
            int amount;
            try {
                if (context.getSlash() == null) {
                    amount = Integer.parseInt(context.getArgs()[0]);
                } else {
                    amount = Integer.parseInt(String.valueOf(context.getSlash().getOption("limit").getAsLong()));
                }
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
