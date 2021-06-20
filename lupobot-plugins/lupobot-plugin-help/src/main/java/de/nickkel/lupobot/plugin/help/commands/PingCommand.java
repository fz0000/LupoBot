package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@CommandInfo(name = "ping", aliases = "pong", category = "general")
public class PingCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.YELLOW.getColor());
        builder.setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
        builder.setTimestamp(context.getTime());

        long ping = context.getMessage().getTimeCreated().until(OffsetDateTime.now(), ChronoUnit.MILLIS);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_ping-ping"), ping + " ms", true);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_ping-websocket"), (long) LupoBot.getInstance().getShardManager().getAverageGatewayPing() + " ms", true);

        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
