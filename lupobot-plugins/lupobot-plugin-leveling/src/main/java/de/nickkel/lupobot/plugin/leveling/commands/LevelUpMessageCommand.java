package de.nickkel.lupobot.plugin.leveling.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

@CommandInfo(name = "levelupmessage", category = "administration", permissions = Permission.ADMINISTRATOR)
public class LevelUpMessageCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length != 0) {
            context.getServer().appendPluginData(context.getPlugin(), "levelUpMessage", context.getArgsAsString());
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_levelupmessage-success", context.getArgsAsString()));
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendSyntaxError(context, "leveling_levelupmessage-empty");
        }
    }
}