package de.nickkel.lupobot.plugin.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.pagination.Page;
import de.nickkel.lupobot.core.pagination.Paginator;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;

@CommandInfo(name = "queue", category = "queue")
public class QueueCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
        if (server.joinedVoiceChannel(context)) {
            ArrayList<Page> pages = new ArrayList<>();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getServer().translate(context.getPlugin(), "music_queue-title"), null,
                    "https://cdn.pixabay.com/photo/2019/08/11/18/27/icon-4399630_960_720.png");
            builder.setTimestamp(context.getTime());

            if (server.getScheduler().getQueue().size() == 0) {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "music_queue-nothing"));
            } else {
                int i = 0;
                for (AudioTrack track : server.getScheduler().getQueue()) {
                    builder.setDescription(builder.getDescriptionBuilder() + "- " + track.getInfo().title + "\n");
                    if (String.valueOf(i).length() != 1 && (String.valueOf(i).endsWith("0") || i == server.getScheduler().getQueue().size()-1)) {
                        Page page = new Page(builder.build());
                        page.getWhitelist().add(context.getMember().getIdLong());
                        pages.add(page);
                        builder.setDescription("");
                    }
                    i++;
                }
            }

            if (pages.size() != 0) {
                Paginator.paginate(context, pages, 60);
            } else {
                send(context, builder);
            }
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}