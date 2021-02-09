package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

@CommandInfo(name = "prefix", permissions = Permission.ADMINISTRATOR, category = "core")
public class PrefixCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1) {
            String prefix = context.getArgs()[0];
            if(context.getServer().getPrefix().equals(prefix)) {
                sendSyntaxError(context, "core_prefix-already-using", prefix);
                return;
            }
            if(prefix.length() <= 10) {
                context.getServer().setPrefix(prefix);
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.GREEN.getColor());
                builder.setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
                builder.setDescription(context.getServer().translate(null, "core_prefix-changed", prefix));
                builder.setTimestamp(context.getMessage().getTimeCreated());
                context.getChannel().sendMessage(builder.build()).queue();
            } else {
                sendSyntaxError(context, "core_prefix-too-long");
            }
        } else {
            sendHelp(context);
        }
    }
}
