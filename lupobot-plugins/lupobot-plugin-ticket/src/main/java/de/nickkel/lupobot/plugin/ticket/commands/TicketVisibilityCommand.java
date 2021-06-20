package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "ticketvisibility", category = "config", permissions = Permission.ADMINISTRATOR)
public class TicketVisibilityCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketvisibility-everyone"));
        builder.setTimestamp(context.getTime());

        boolean visibleEveryone = (boolean) context.getServer().getPluginData(context.getPlugin(), "visibleEveryone");
        if (visibleEveryone) {
            context.getServer().appendPluginData(context.getPlugin(), "visibleEveryone", false);
            builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketvisibility-private"));
            builder.setColor(LupoColor.RED.getColor());
        } else {
            context.getServer().appendPluginData(context.getPlugin(), "visibleEveryone", true);
            builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketvisibility-everyone"));
            builder.setColor(LupoColor.GREEN.getColor());
        }
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
