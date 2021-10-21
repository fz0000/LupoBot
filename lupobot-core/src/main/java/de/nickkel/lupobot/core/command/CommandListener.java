package de.nickkel.lupobot.core.command;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        LupoServer server = LupoServer.getByGuild(event.getGuild());
        String prefix = server.getPrefix();

        if (event.getMessage().getContentRaw().replace("!", "").startsWith(LupoBot.getInstance().getSelfUser().getAsMention())) {
            LupoCommand command = LupoBot.getInstance().getCommand("help");
            if (command != null) {
                CommandContext context = new CommandContext(event.getGuild(), event.getMember(), event.getChannel(), event.getMessage(), "prefix", new String[]{}, null, server.isSlashInvisible());
                context.setPlugin(LupoBot.getInstance().getPlugin("help"));
                command.onCommand(context);
            }
        }

        if (!event.getMessage().getContentRaw().startsWith(prefix) || event.getMessage().getContentRaw().equals(prefix)) {
            return;
        }

        String label = event.getMessage().getContentStripped().replace(prefix, "").split(" ")[0];
        String message = event.getMessage().getContentRaw().replaceFirst(Pattern.quote(prefix), "").replaceFirst(label, "");

        if (message.startsWith(" ")) {
            message = message.substring(1);
        }

        String[] args = message.split(" ");
        if (Arrays.toString(args).equals("[]")) {
            args = new String[0];
        }
        CommandContext context = new CommandContext(event.getGuild(), event.getMember(), event.getChannel(), event.getMessage(), label, args, null, server.isSlashInvisible());
        LupoBot.getInstance().getCommandHandler().runCommand(context);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getGuild() == null) {
            event.reply("Commands can only be used on guilds which have invited LupoBot!").queue();
            return;
        }
        LupoBot.getInstance().getCommandHandler().runCommand(new CommandContext(event.getGuild(), event.getMember(), event.getTextChannel(),
                null, event.getName(), new String[]{}, event, LupoServer.getByGuild(event.getGuild()).isSlashInvisible()));
    }
}
