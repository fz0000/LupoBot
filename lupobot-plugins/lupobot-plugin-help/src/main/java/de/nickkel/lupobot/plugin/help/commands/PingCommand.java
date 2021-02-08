package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@CommandInfo(name = "ping", aliases = "pong", category = "general")
public class PingCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.YELLOW.getColor());
        builder.setAuthor(LupoBot.getInstance().getJda().getSelfUser().getName(), null, LupoBot.getInstance().getJda().getSelfUser().getAvatarUrl());
        builder.setTimestamp(LocalDateTime.now());

        long ping = context.getMessage().getTimeCreated().until(context.getMessage().getTimeCreated(), ChronoUnit.MILLIS);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_ping-ping") + " ms", String.valueOf(ping), true);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_ping-websocket") + " ms", String.valueOf(LupoBot.getInstance().getJda().getGatewayPing()), true);

        context.getChannel().sendMessage(builder.build()).queue();
    }
}
