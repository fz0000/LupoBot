package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "guildinfo", category = "information")
public class GuildinfoCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String emotes = "/";
        for (Emote emote : context.getGuild().getEmotes()) {
            emotes = emotes + emote.getAsMention() + " ";
        }

        String afkChannel = "/";
        if (context.getGuild().getAfkChannel() != null) {
            afkChannel = context.getGuild().getAfkChannel().getName();
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getTime());
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getIdLong() + ")", null, context.getGuild().getIconUrl());

        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-creation"), TimeUtils.format(context.getGuild().getTimeCreated()), true);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-members"), String.valueOf(context.getGuild().getMemberCount()), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-region"), context.getGuild().getRegion().getName(), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-owner"), context.getGuild().getOwner().getAsMention(), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-channels"), String.valueOf(context.getGuild().getChannels().size()), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-verification"), context.getGuild().getVerificationLevel().name(), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-afkchannel"), afkChannel, false);
        builder.addField(context.getServer().translate(context.getPlugin(), "help_guildinfo-emotes"), emotes, false);

        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
