package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandInfo(name = "help", category = "general")
public class HelpCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1) {
            LupoCommand command = null;
            for (LupoCommand all : LupoBot.getInstance().getCommands()) {
                if (context.getArgs()[0].equalsIgnoreCase(all.getInfo().name())) {
                    command = all;
                }

                for (String alias : all.getInfo().aliases()) {
                    if (context.getArgs()[0].equalsIgnoreCase(alias)) {
                        command = all;
                    }
                }
            }

            if(command == null) {
                sendHelp(context);
                return;
            }

            CommandContext helpContext = new CommandContext(context.getMember(), context.getChannel(), context.getMessage(), command.getInfo().name(), new String[0]);
            LupoPlugin plugin = null;
            for(LupoPlugin all : LupoBot.getInstance().getPlugins()) {
                if(all.getCommands().contains(command)) {
                    plugin = all;
                }
            }
            helpContext.setPlugin(plugin);
            command.sendHelp(helpContext);
        } else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-invite"),
                    LupoBot.getInstance().getConfig().getString("inviteUrl"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-server"),
                    LupoBot.getInstance().getConfig().getString("supportServerUrl"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-all-plugins"),
                    context.getServer().translate(context.getPlugin(), "help_help-all-plugins-description"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-all-commands"),
                    context.getServer().translate(context.getPlugin(), "help_help-all-commands-description"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-all-details"),
                    context.getServer().translate(context.getPlugin(), "help_help-all-details-description"), false);
            context.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
