package de.nickkel.lupobot.plugin.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "queue", category = "queue")
public class QueueCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
        if (server.joinedVoiceChannel(context)) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getServer().translate(context.getPlugin(), "music_queue-title"), null,
                    "https://cdn.pixabay.com/photo/2019/08/11/18/27/icon-4399630_960_720.png");
            builder.setTimestamp(context.getTime());

            if (server.getScheduler().getQueue().size() == 0) {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "music_queue-nothing"));
            } else {
                String description = "";
                for (AudioTrack track : server.getScheduler().getQueue()) {
                    description = description + "- " + track.getInfo().title + "\n";
                }
                builder.setDescription(description);
            }
            send(context, builder);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}