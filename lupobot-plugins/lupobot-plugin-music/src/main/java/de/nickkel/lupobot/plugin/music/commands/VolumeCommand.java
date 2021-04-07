package de.nickkel.lupobot.plugin.music.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

@CommandInfo(name = "volume", category = "player", permissions = Permission.MANAGE_SERVER)
public class VolumeCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1) {
            try {
                MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
                if (!server.joinedVoiceChannel(context)) {
                    return;
                }
                int volume = Integer.parseInt(context.getArgs()[0]);
                if (volume > 0 && volume < 101) {
                    context.getServer().appendPluginData(context.getPlugin(), "volume", volume);
                    server.getAudioPlayer().setVolume(volume);
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(LupoColor.GREEN.getColor());
                    builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                            context.getMember().getUser().getAvatarUrl());
                    builder.setDescription(context.getServer().translate(context.getPlugin(), "music_volume-changed", volume));
                    builder.setTimestamp(context.getMessage().getTimeCreated());
                    context.getChannel().sendMessage(builder.build()).queue();
                } else {
                    sendSyntaxError(context, "music_volume-range");
                }
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "music_volume-only-numbers");
            }
        } else {
            sendHelp(context);
        }
    }
}
