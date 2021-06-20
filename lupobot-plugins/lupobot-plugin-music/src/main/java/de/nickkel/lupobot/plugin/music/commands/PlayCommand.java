package de.nickkel.lupobot.plugin.music.commands;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.ThrowingBiConsumer;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "play", category = "player")
@SlashOption(name = "input", type = OptionType.STRING)
public class PlayCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length >= 1 || context.getSlash() != null) {
            String arg;
            if (context.getSlash() == null) {
                arg = context.getArgsAsString();
            } else {
                arg = context.getSlash().getOption("input").getAsString();
            }
            MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
            if (!server.joinedVoiceChannel(context)) {
                return;
            }
            if (arg.startsWith("http") && arg.contains("/")) {
                context.getSlash().deferReply().queue();
                server.play(this, context, arg);
            } else {
                LupoMusicPlugin.getInstance().getAudioPlayerManager().loadItemOrdered(server, "ytsearch: " + arg, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        context.getSlash().deferReply().queue();
                        server.scheduler.queue(audioTrack);
                        server.onQueue(context, audioTrack);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        List<AudioTrack> tracks = audioPlaylist.getTracks();
                        Map<Integer, ThrowingBiConsumer<Member, Message>> consumers = new HashMap<>();

                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(LupoColor.ORANGE.getColor());
                        builder.setAuthor(context.getServer().translate(context.getPlugin(), "music_play-song-selection"), null,
                                "https://cdn.pixabay.com/photo/2019/08/11/18/27/icon-4399630_960_720.png");
                        String description = "";

                        for (int i=1; i < 10; i++) {
                            try {
                                AudioTrack track = tracks.get(i-1);
                                description = description + "**" + i + ":** " + track.getInfo().title + "\n";

                                ThrowingBiConsumer<Member, Message> consumer = (member, message) -> {
                                    server.scheduler.queue(track);
                                    server.onQueue(context, track);
                                };
                                consumers.put(i, consumer);
                            } catch (IndexOutOfBoundsException e) {
                                break;
                            }
                        }
                        builder.setDescription(description);
                        builder.setTimestamp(context.getTime());
                        context.setEphemeral(false);
                        context.getSlash().deferReply().queue();
                        context.getChannel().sendMessage(builder.build()).queue(success -> {
                            HashMap<String, ThrowingBiConsumer<Member, Message>> buttons = new HashMap<>();
                            for (int i = 1; i < consumers.size()+1; i++) {
                                buttons.put(getEmoji(i), consumers.get(i));
                            }
                            Pages.buttonize(success, buttons, false, 60, TimeUnit.SECONDS, user -> context.getUser().getId() == user.getIdLong());
                        });
                    }

                    @Override
                    public void noMatches() {
                        sendSyntaxError(context, "music_no-matches-search");
                    }

                    @Override
                    public void loadFailed(FriendlyException e) {
                        sendSyntaxError(context, "music_load-failed-search");
                    }
                });
            }
        } else {
            sendHelp(context);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }

    private String getEmoji(int number) {
        switch(number) {
            case 2:
                return "2️⃣";
            case 3:
                return "3️⃣";
            case 4:
                return "4️⃣";
            case 5:
                return "5️⃣";
            case 6:
                return "6️⃣";
            case 7:
                return "7️⃣";
            case 8:
                return "8️⃣";
            case 9:
                return "9️⃣";
            default:
                return "1️⃣";
        }
    }
}
