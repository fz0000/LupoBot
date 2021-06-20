package de.nickkel.lupobot.plugin.music.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.music.LupoMusicPlugin;
import de.nickkel.lupobot.plugin.music.lavaplayer.MusicServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "volume", category = "player", permissions = Permission.MANAGE_SERVER)
@SlashOption(name = "volume", type = OptionType.INTEGER)
public class VolumeCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1 || context.getSlash() != null) {
            try {
                MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
                if (!server.joinedVoiceChannel(context)) {
                    return;
                }
                int volume;
                if (context.getSlash() == null) {
                    volume = Integer.parseInt(context.getArgs()[0]);
                } else {
                    volume = Integer.parseInt(String.valueOf(context.getSlash().getOption("volume").getAsLong()));
                }
                if (volume > 0 && volume < 101) {
                    context.getServer().appendPluginData(context.getPlugin(), "volume", volume);
                    server.getAudioPlayer().setVolume(volume);
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(LupoColor.GREEN.getColor());
                    builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                            context.getMember().getUser().getAvatarUrl());
                    builder.setDescription(context.getServer().translate(context.getPlugin(), "music_volume-changed", volume));
                    builder.setTimestamp(context.getTime());
                    send(context, builder);
                } else {
                    sendSyntaxError(context, "music_volume-range");
                }
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "music_volume-only-numbers");
            }
        } else {
            MusicServer server = LupoMusicPlugin.getInstance().getMusicServer(context.getGuild());
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getServer().translate(context.getPlugin(), "music_volume-title"), null,
                    "https://cdn.pixabay.com/photo/2019/08/11/18/27/icon-4399630_960_720.png");
            builder.setDescription(context.getServer().translate(context.getPlugin(), "music_current-volume", server.getVolume()));
            builder.setTimestamp(context.getTime());
            send(context, builder);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
