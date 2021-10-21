package de.nickkel.lupobot.core.internal.commands;

import com.mongodb.BasicDBList;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "togglecommand", permissions = Permission.ADMINISTRATOR, category = "core")
@SlashOption(name = "command", type = OptionType.STRING)
public class ToggleCommandCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1 || context.getSlash() != null) {
            LupoServer server = LupoServer.getByGuild(context.getGuild());
            String command;
            if (context.getSlash() == null) {
                command = context.getArgs()[0];
            } else {
                command = context.getSlash().getOption("command").getAsString();
            }

            if (LupoBot.getInstance().getCommand(command) == null) {
                sendSyntaxError(context, "core_togglecommand-unknown");
                return;
            }

            if (command.equalsIgnoreCase("togglecommand")) {
                sendSyntaxError(context, "core_togglecommand-fail");
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getTime());
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.addField(context.getServer().translate(null, "core_command"), LupoBot.getInstance().getCommand(command).getInfo().name(), true);
            builder.addField(context.getServer().translate(null, "core_plugin"), context.getServer().translatePluginName(LupoBot.getInstance().getCommand(command).getPlugin()), true);

            BasicDBList disabledCommands = (BasicDBList) server.getData().get("disabledCommands");
            if (disabledCommands.contains(command)) {
                disabledCommands.remove(command);
                builder.setColor(LupoColor.GREEN.getColor());
                builder.setDescription(context.getServer().translate(null, "core_togglecommand-activated", LupoBot.getInstance().getCommand(command).getInfo().name()));
            } else {
                disabledCommands.add(command);
                builder.setColor(LupoColor.RED.getColor());
                builder.setDescription(context.getServer().translate(null, "core_togglecommand-disabled", LupoBot.getInstance().getCommand(command).getInfo().name()));
            }
            send(context, builder);
        } else {
            sendHelp(context);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
