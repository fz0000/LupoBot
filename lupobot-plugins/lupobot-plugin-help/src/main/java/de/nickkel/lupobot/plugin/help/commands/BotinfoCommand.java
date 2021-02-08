package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.concurrent.TimeUnit;

@CommandInfo(name = "botinfo", category = "information")
public class BotinfoCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        long runtime = System.currentTimeMillis()-LupoBot.getInstance().getStartMilis();
        String time = String.format("%d " + context.getServer().translate(null, "core_command-cooldown-minutes") + ", %d " + context.getServer().translate(null, "core_command-cooldown-seconds"),
                TimeUnit.MILLISECONDS.toMinutes(runtime), TimeUnit.MILLISECONDS.toSeconds(runtime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runtime))
        );
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-runtime"),
                context.getServer().translate(context.getPlugin(), "help_botinfo-runtime-value", time), true);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-stats"),
                context.getServer().translate(context.getPlugin(), "help_botinfo-stats-value", LupoBot.getInstance().getShardManager().getGuilds().size(), LupoBot.getInstance().getShardManager().getShardsTotal()), true);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-version"),
                LupoBot.getInstance().getClass().getPackage().getImplementationVersion(), true);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-support"),
                LupoBot.getInstance().getConfig().getString("supportServerUrl"), true);

        builder.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-invite"),
                LupoBot.getInstance().getConfig().getString("inviteUrl"), true);

        context.getChannel().sendMessage(builder.build()).queue();
    }
}
