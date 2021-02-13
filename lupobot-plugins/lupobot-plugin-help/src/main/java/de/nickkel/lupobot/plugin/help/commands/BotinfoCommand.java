package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.concurrent.TimeUnit;

@CommandInfo(name = "botinfo", category = "information")
public class BotinfoCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        long runtime = System.currentTimeMillis()-LupoBot.getInstance().getStartMilis();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-runtime"),
                context.getServer().translate(context.getPlugin(), "help_botinfo-runtime-value", TimeUtils.format(context, runtime)), false);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-stats"),
                context.getServer().translate(context.getPlugin(), "help_botinfo-stats-value",
                        LupoBot.getInstance().getShardManager().getGuilds().size(), LupoBot.getInstance().getShardManager().getShardsTotal()), false);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-version"),
                LupoBot.getInstance().getClass().getPackage().getImplementationVersion(), false);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-support"),
                LupoBot.getInstance().getConfig().getString("supportServerUrl"), false);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-invite"),
                LupoBot.getInstance().getConfig().getString("inviteUrl"), false);

        context.getChannel().sendMessage(builder.build()).queue();
    }
}
