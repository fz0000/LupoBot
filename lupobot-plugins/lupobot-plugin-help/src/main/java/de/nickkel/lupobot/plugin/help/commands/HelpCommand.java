package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.Button;

@CommandInfo(name = "help", category = "general")
@SlashOption(name = "command", type = OptionType.STRING, required = false)
public class HelpCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1 || (context.getSlash() != null && context.getSlash().getOption("command") != null)) {
            String commandName;
            if (context.getSlash() != null) {
                commandName = context.getSlash().getOption("command").getAsString();
            } else {
                commandName = context.getArgs()[0];
            }
            LupoCommand command = null;
            for (LupoCommand all : LupoBot.getInstance().getCommands()) {
                if (commandName.equalsIgnoreCase(all.getInfo().name())) {
                    command = all;
                }

                for (String alias : all.getInfo().aliases()) {
                    if (commandName.equalsIgnoreCase(alias)) {
                        command = all;
                    }
                }
            }

            if (command == null) {
                sendHelp(context);
                return;
            }

            CommandContext helpContext = new CommandContext(context.getGuild(), context.getMember(), context.getChannel(), context.getMessage(), command.getInfo().name(), new String[0], context.getSlash(), context.isEphemeral());
            LupoPlugin plugin = null;
            for (LupoPlugin all : LupoBot.getInstance().getPlugins()) {
                if (all.getCommands().contains(command)) {
                    plugin = all;
                }
            }
            helpContext.setPlugin(plugin);
            command.sendHelp(helpContext);
        } else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
            
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-invite"),
                    LupoBot.getInstance().getConfig().getString("inviteUrl"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-server"),
                    LupoBot.getInstance().getConfig().getString("supportServerUrl"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-all-plugins"),
                    context.getServer().translate(context.getPlugin(), "help_help-all-plugins-description"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-install-plugins"),
                    context.getServer().translate(context.getPlugin(), "help_help-install-plugins-description"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-all-commands"),
                    context.getServer().translate(context.getPlugin(), "help_help-all-commands-description"), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "help_help-all-details"),
                    context.getServer().translate(context.getPlugin(), "help_help-all-details-description"), false);
            
            send(context, builder,
                    Button.link(LupoBot.getInstance().getConfig().getString("inviteUrl"), context.getServer().translate(context.getPlugin(), "help_help-link-invite")),
                    Button.link(LupoBot.getInstance().getConfig().getString("supportServerUrl"), context.getServer().translate(context.getPlugin(), "help_help-link-support"))
            );
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
