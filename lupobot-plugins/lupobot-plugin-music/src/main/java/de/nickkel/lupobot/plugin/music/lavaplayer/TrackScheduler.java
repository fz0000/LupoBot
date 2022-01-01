package de.nickkel.lupobot.plugin.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    @Getter
    private final MusicServer server;
    @Getter
    private final AudioPlayer player;
    @Getter
    private final BlockingQueue<AudioTrack> queue;
    @Getter
    private final List<Member> voteSkip = new ArrayList<>();

    public TrackScheduler(AudioPlayer player, MusicServer server) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.server = server;
    }

    public void next() {
        this.voteSkip.clear();
        this.player.startTrack(this.queue.poll().makeClone(), false);
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            next();
        } else {
            this.server.getGuild().getAudioManager().closeAudioConnection();
        }
    }
}