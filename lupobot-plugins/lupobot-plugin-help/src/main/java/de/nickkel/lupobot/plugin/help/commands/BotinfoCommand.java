package de.nickkel.lupobot.plugin.help.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "botinfo", category = "information")
public class BotinfoCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        long runtime = System.currentTimeMillis()-LupoBot.getInstance().getStartMillis();
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(LupoColor.BLUE.getColor())
                .setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl())
                .setTimestamp(context.getTime())
                .addField(context.getServer().translate(context.getPlugin(), "help_botinfo-runtime"),
                        context.getServer().translate(context.getPlugin(), "help_botinfo-runtime-value", TimeUtils.format(context, runtime)), false)
                .addField(context.getServer().translate(context.getPlugin(), "help_botinfo-stats"),
                        context.getServer().translate(context.getPlugin(), "help_botinfo-stats-value",
                                LupoBot.getInstance().getShardManager().getGuilds().size(), LupoBot.getInstance().getShardManager().getShardsTotal()), false)
                /*.addField(context.getServer().translate(context.getPlugin(), "help_botinfo-version"),
                        LupoBot.getInstance().getClass().getPackage().getImplementationVersion(), false)*/
                .addField(context.getServer().translate(context.getPlugin(), "help_botinfo-support"),
                        LupoBot.getInstance().getConfig().getString("supportServerUrl"), false)
                .addField(context.getServer().translate(context.getPlugin(), "help_botinfo-invite"),
                        LupoBot.getInstance().getConfig().getString("inviteUrl"), false);

        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
