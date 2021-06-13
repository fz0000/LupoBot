package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.*;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "prefix", permissions = Permission.ADMINISTRATOR, category = "core")
@SlashOption(name = "text", type = OptionType.STRING)
public class PrefixCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1 || context.getSlash() != null) {
            String prefix;
            if (context.getSlash() == null) {
                prefix = context.getArgs()[0];
            } else {
                prefix = context.getSlash().getOption("text").getAsString();
                if (prefix.contains(" ")) {
                    sendHelp(context);
                    return;
                }
            }
            if(context.getServer().getPrefix().equals(prefix)) {
                sendSyntaxError(context, "core_prefix-already-using", prefix);
                return;
            }
            if (prefix.length() <= 10) {
                context.getServer().setPrefix(prefix);
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.GREEN.getColor());
                builder.setAuthor(LupoBot.getInstance().getSelfUser().getName() + " (" + context.getGuild().getId() + ")", null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
                builder.setDescription(context.getServer().translate(null, "core_prefix-changed", prefix));
                builder.setTimestamp(context.getTime());
                send(context, builder);
            } else {
                sendSyntaxError(context, "core_prefix-too-long");
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
