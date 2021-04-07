package de.nickkel.lupobot.plugin.music.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

@CommandInfo(name = "skip", category = "skip")
public class SkipCommand extends LupoCommand {
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
            builder.setDescription(context.getServer().translate(context.getPlugin(), "music_skip-nothing"));
        } else {
            int members = 0;
            for (Member member : context.getMember().getVoiceState().getChannel().getMembers()) {
                if(!member.getUser().isBot()) {
                    members++;
                }
            }

            if (members == 1) {
                server.getScheduler().next();
                builder.setDescription(context.getServer().translate(context.getPlugin(), "music_skip-only-user"));
            } else {
                if (server.getScheduler().getVoteSkip().contains(context.getMember())) {
                    builder.setColor(LupoColor.RED.getColor());
                    builder.setDescription(context.getServer().translate(context.getPlugin(), "music_already-voted"));
                    context.getChannel().sendMessage(builder.build()).queue();
                    return;
                }
                server.getScheduler().getVoteSkip().add(context.getMember());
                if (server.getScheduler().getVoteSkip().size() == members) {
                    server.getScheduler().next();
                    builder.setDescription(context.getServer().translate(context.getPlugin(), "music_skip-success",
                            server.getAudioPlayer().getPlayingTrack().getInfo().title, server.getScheduler().getVoteSkip().size(), members));
                } else {
                    builder.setDescription(context.getServer().translate(context.getPlugin(), "music_skip-voted",
                            members, members));
                }
            }
        }

        context.getChannel().sendMessage(builder.build()).queue();
    }
}
