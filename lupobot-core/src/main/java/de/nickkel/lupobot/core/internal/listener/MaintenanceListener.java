package de.nickkel.lupobot.core.internal.listener;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MaintenanceListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().startsWith("?")) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(event.getMember().getUser().getAsTag() + " (" + event.getMember().getId() + ")", null,
                    event.getMember().getUser().getAvatarUrl());
            builder.setDescription("Sorry for the inconvenience but we are performing some maintenance at the moment. We will be back online shortly, please be patient!");
            builder.addField("If you have any questions, feel free to join our Discord server:", LupoBot.getInstance().getConfig().getString("supportServerUrl"), false);
            builder.setColor(LupoColor.RED.getColor());
            builder.setTimestamp(event.getMessage().getTimeCreated());
            event.getChannel().sendMessage(builder.build()).queue();
        }
    }
}