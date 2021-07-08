package de.nickkel.lupobot.plugin.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

@PluginInfo(name = "music", version = "1.0.0", author = "Nickkel")
public class LupoMusicPlugin extends LupoPlugin {

    @Getter
    public static LupoMusicPlugin instance;
    @Getter
    private AudioPlayerManager audioPlayerManager;
    private final Map<Long, MusicServer> musicServer = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        this.registerCommands("de.nickkel.lupobot.plugin.music.commands");
    }

    @Override
    public void onDisable() {

    }

    public MusicServer getMusicServer(Guild guild) {
        return this.musicServer.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final MusicServer server = new MusicServer(this.audioPlayerManager, LupoBot.getInstance().getShardManager().getGuildById(guildId));
            guild.getAudioManager().setSendingHandler(server.getSendHandler());
            return server;
        });
    }
}
