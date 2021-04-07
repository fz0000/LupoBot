package de.nickkel.lupobot.plugin.music.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

@CommandInfo(name = "forceskip", category = "skip", permissions = Permission.MANAGE_SERVER)
public class ForceSkipCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
        if (!server.joinedVoiceChannel(context)) {
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.ORANGE.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                context.getMember().getUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated());

        if (server.getScheduler().getQueue().size() == 0) {
            builder.setDescription(context.getServer().translate(context.getPlugin(), "music_forceskip-nothing"));
        } else {
            server.getScheduler().next();
            builder.setDescription(context.getServer().translate(context.getPlugin(), "music_forceskip-success", server.getAudioPlayer().getPlayingTrack().getInfo().title));
        }

        context.getChannel().sendMessage(builder.build()).queue();
    }
}
