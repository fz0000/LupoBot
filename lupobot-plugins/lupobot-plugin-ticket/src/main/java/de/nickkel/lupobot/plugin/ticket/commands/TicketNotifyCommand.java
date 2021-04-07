package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

@CommandInfo(name = "ticketnotify", category = "config", permissions = Permission.ADMINISTRATOR)
public class TicketNotifyCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {

        if (context.getArgs().length == 1) {
            TextChannel channel = context.getServer().getTextChannel(context.getArgs()[0]);
            if (channel == null) {
                sendSyntaxError(context, "ticket_ticketnotify-invalid-channel");
                return;
            }
            context.getServer().appendPluginData(context.getPlugin(), "notifyChannel", channel.getIdLong());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketnotify-info"));
            builder.addField(context.getServer().translate(context.getPlugin(), "ticket_ticketnotify-channel"),
                    channel.getAsMention() + " (" + channel.getId() + ")", false);
            builder.setTimestamp(context.getMessage().getTimeCreated());
            builder.setColor(LupoColor.GREEN.getColor());
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendHelp(context);
        }
    }
}
