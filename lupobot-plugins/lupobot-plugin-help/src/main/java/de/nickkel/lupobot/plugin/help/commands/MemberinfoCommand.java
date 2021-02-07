package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CommandInfo(name = "memberinfo", category = "information")
public class MemberinfoCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String roles = "/";
        for(Role role : context.getMember().getRoles()) {
            roles = roles + role.getName() + ", ";
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(LocalDateTime.now());
        builder.setColor(Color.decode("#0066CC"));
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());

        builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-creation"), context.getMember().getUser().getTimeCreated().format(DateTimeFormatter.ISO_DATE_TIME), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-joined"), context.getMember().getTimeJoined().format(DateTimeFormatter.ISO_DATE_TIME), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-status"), context.getMember().getOnlineStatus().name(), false);
        //builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-activity"), String.valueOf(context.getMember().getActivities().get(0)), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-roles"), roles, false);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}