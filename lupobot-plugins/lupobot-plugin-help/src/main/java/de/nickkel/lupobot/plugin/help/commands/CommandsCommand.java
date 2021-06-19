package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandInfo(name = "commands", category = "general", aliases = "cmds")
@SlashOption(name = "plugin", type = OptionType.STRING)
public class CommandsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {

        if (context.getArgs().length == 1 || context.getSlash() != null) {
            String name;
            if (context.getSlash() != null) {
                name = context.getSlash().getOption("plugin").getAsString();
            } else {
                name = context.getArgs()[0];
            }
            boolean match = false;
            int plugins = 0;
            for (LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
                plugins++;
                if (name.equalsIgnoreCase(plugin.getInfo().name()) || name.equalsIgnoreCase(context.getServer().translatePluginName(plugin))) {
                    match = true;
                    List<String> categories = new ArrayList<>();
                    Map<LupoCommand, String> commands = new HashMap<>();

                    for (LupoCommand command : plugin.getCommands()) {
                        commands.put(command, command.getInfo().category());
                        if (!categories.contains(command.getInfo().category()) && !command.getInfo().hidden()) {
                            categories.add(command.getInfo().category());
                        }
                    }

                    List<MessageEmbed.Field> fields = new ArrayList<>();
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(LupoColor.ORANGE.getColor());
                    builder.setTitle(context.getServer().translate(context.getPlugin(), "help_commands-plugin", context.getServer().translatePluginName(plugin)));

                    for (String category : categories) {
                        String title = context.getServer().translate(plugin, plugin.getInfo().name() + "_category-" + category);
                        String value = "";
                        for (LupoCommand command : commands.keySet()) {
                            if (command.getInfo().category().equalsIgnoreCase(category)) {
                                value = value + context.getServer().getPrefix() + command.getInfo().name() + " :: " +
                                        context.getServer().translate(plugin, plugin.getInfo().name() + "_" + command.getInfo().name() + "-description") + "\n";
                            }
                        }
                        fields.add(new MessageEmbed.Field(title, "```asciidoc\n" + value + "```", false));
                    }

                    for (MessageEmbed.Field field : fields) {
                        builder.addField(field);
                    }

                    builder.setFooter(context.getServer().translate(context.getPlugin(),"help_commands-footer"));
                    send(context, builder);
                }
            }

            if (!match && plugins == LupoBot.getInstance().getPlugins().size()) {
                sendSyntaxError(context, "help_commands-invalid-plugin");
            }
        } else {
            sendHelp(context);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
