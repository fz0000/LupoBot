package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "ticketnotify", category = "config", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "channel", type = OptionType.CHANNEL)
public class TicketNotifyCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1 || context.getSlash() != null) {
            TextChannel channel;
            if (context.getSlash() == null) {
                channel = context.getServer().getTextChannel(context.getArgs()[0]);
            } else {
                channel = (TextChannel) context.getSlash().getOption("channel").getAsMessageChannel();
            }
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
