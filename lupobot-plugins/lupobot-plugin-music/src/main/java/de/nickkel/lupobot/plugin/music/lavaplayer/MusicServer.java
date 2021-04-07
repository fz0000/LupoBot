package de.nickkel.lupobot.plugin.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;

public class MusicServer {

    @Getter
    public final AudioPlayer audioPlayer;
    @Getter
    public final TrackScheduler scheduler;
    @Getter
    private final AudioPlayerSendHandler sendHandler;
    @Getter
    private final Guild guild;
    @Getter
    private final LupoServer server;

    public MusicServer(AudioPlayerManager manager, Guild guild) {
        this.guild = guild;
        this.server = LupoServer.getByGuild(this.guild);
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void play(LupoCommand command, CommandContext context, String trackUrl) {
        LupoMusicPlugin.getInstance().getAudioPlayerManager().loadItemOrdered(this, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                MusicServer.this.scheduler.queue(audioTrack);
                onQueue(context, audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                List<AudioTrack> tracks = audioPlaylist.getTracks();
                for (AudioTrack track : tracks) {
                    MusicServer.this.scheduler.queue(track);
                }
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.GREEN.getColor());
                builder.setTimestamp(context.getMessage().getTimeCreated());
                builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
                builder.setDescription(context.getServer().translate(context.getPlugin(), "music_loaded-playlist", audioPlaylist.getName(), audioPlaylist.getTracks().size()));
                context.getChannel().sendMessage(builder.build()).queue();
            }

            @Override
            public void noMatches() {
                command.sendSyntaxError(context, "music_no-matches");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                command.sendSyntaxError(context, "music_load-failed");
            }
        });
    }

    public boolean joinedVoiceChannel(CommandContext context) {
        GuildVoiceState memberVoiceState = context.getMember().getVoiceState();
        GuildVoiceState selfVoiceState = this.guild.getSelfMember().getVoiceState();
        AudioManager audioManager = this.guild.getAudioManager();

        if (!memberVoiceState.inVoiceChannel()) {
            context.getChannel().sendMessage(this.server.translate(context.getPlugin(), "music_member-not-in-voicechannel",
                    context.getMember().getAsMention())).queue();
            return false;
        }
        if (selfVoiceState.inVoiceChannel() && memberVoiceState.getChannel().getIdLong() != selfVoiceState.getChannel().getIdLong()) {
            context.getChannel().sendMessage(this.server.translate(context.getPlugin(), "music_bot-already-in-voicechannel",
                    context.getMember().getAsMention())).queue();
            return false;
        }

        audioManager.openAudioConnection(memberVoiceState.getChannel());
        return true;
    }

    public void onQueue(CommandContext context, AudioTrack track) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.GREEN.getColor());
        builder.setTimestamp(context.getMessage().getTimeCreated());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "music_queued-track"));
        builder.addField(context.getServer().translate(context.getPlugin(), "music_queued-track-title"), track.getInfo().title, true);
        builder.addField(context.getServer().translate(context.getPlugin(), "music_queued-track-duration"), TimeUtils.format(context, track.getDuration()), true);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}