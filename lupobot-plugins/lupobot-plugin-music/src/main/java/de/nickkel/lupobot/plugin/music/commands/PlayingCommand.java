package de.nickkel.lupobot.plugin.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandInfo(name = "playing", category = "player")
public class PlayingCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
        if (server.joinedVoiceChannel(context)) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getServer().translate(context.getPlugin(), "music_playing-title"), null,
                    "https://cdn.pixabay.com/photo/2019/08/11/18/27/icon-4399630_960_720.png");
            builder.setTimestamp(context.getMessage().getTimeCreated());

            if (server.getAudioPlayer().getPlayingTrack() == null) {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "music_playing-nothing"));
            } else {
                AudioTrack track = server.getAudioPlayer().getPlayingTrack();
                builder.addField(context.getServer().translate(context.getPlugin(), "music_queued-track-title"),
                        server.getAudioPlayer().getPlayingTrack().getInfo().title, false);
                builder.addField(context.getServer().translate(context.getPlugin(), "music_queued-track-duration"),
                        TimeUtils.format(context, track.getDuration()), false);
            }
            context.getChannel().sendMessage(builder.build()).queue();
        }
    }
}