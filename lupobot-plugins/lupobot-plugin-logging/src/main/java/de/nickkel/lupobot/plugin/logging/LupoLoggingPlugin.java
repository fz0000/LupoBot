package de.nickkel.lupobot.plugin.logging;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.core.util.ListenerRegister;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

@PluginInfo(name = "logging", author = "Nickkel")
public class LupoLoggingPlugin extends LupoPlugin {

    @Getter
    public static LupoLoggingPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands("de.nickkel.lupobot.plugin.logging.commands");
        this.registerListeners("de.nickkel.lupobot.plugin.logging.listener");
    }

    @Override
    public void onDisable() {

    }

    public void sendLog(LogEvent event, Guild guild, EmbedBuilder builder) {
        long channelId = LupoLoggingPlugin.getInstance().getChannelId(event, guild);
        if (channelId != -1) {
            TextChannel channel = guild.getTextChannelById(channelId);
            builder.setDescription(LupoServer.getByGuild(guild).translate(LupoBot.getInstance().getPlugin(this.getInfo().name()), event.getLocale()));
            channel.sendMessage(builder.build()).queue();
        }
    }

    public long getChannelId(LogEvent event, Guild guild) {
        LupoServer server = LupoServer.getByGuild(guild);
        long channelId = Long.parseLong(server.getPluginData(LupoBot.getInstance().getPlugin(this.getInfo().name()), event.getKey()).toString());
        if (channelId != -1) {
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel == null) {
                server.appendPluginData(LupoBot.getInstance().getPlugin(this.getInfo().name()), event.getKey(), -1);
                return -1;
            }
        }
        return channelId;
    }
}
