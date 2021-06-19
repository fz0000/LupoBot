package de.nickkel.lupobot.plugin.leveling.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "levelupmessage", category = "administration", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "message", type = OptionType.STRING)
public class LevelUpMessageCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length != 0 || context.getSlash() != null) {
            String args = context.getArgsAsString();
            if (context.getSlash() != null) {
                args = context.getSlash().getOption("message").getAsString();
            }
            context.getServer().appendPluginData(context.getPlugin(), "levelUpMessage", args);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_levelupmessage-success", args));
            send(context, builder);
        } else {
            sendSyntaxError(context, "leveling_levelupmessage-empty");
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}