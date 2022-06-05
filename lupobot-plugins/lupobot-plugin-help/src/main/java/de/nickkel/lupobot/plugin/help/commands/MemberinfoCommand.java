package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "memberinfo", category = "information")

public class MemberinfoCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        Member member = context.getMember();

        if (context.getArgs().length == 1) {
            member = context.getServer().getMember(context.getArgs()[0]);
            if (member == null) {
                sendSyntaxError(context, "help_memberinfo-not-found");
                return;
            }
        }

        String roles = "/";
        if (member.getRoles().size() != 0) {
            roles = "";
        }
        for (Role role : member.getRoles()) {
            roles = roles + role.getName() + ", ";
        }
        if (!roles.equals("/")) {
            roles = roles.substring(0, roles.length() - 2);
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(member.getUser().getAsTag() + " (" + member.getIdLong() + ")", null, member.getUser().getAvatarUrl());

        builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-creation"), TimeUtils.format(member.getUser().getTimeCreated()), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-joined"), TimeUtils.format(member.getTimeJoined()), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_memberinfo-roles"), roles, false);
        context.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {

    }
}