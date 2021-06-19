package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "daily", aliases = "d", category = "reward")
public class DailyCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        long lastDailyCoins = context.getUser().getPluginLong(context.getPlugin(), "lastDailyCoins");
        if (lastDailyCoins != -1 && lastDailyCoins+86400000-System.currentTimeMillis() > 0) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.RED.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                    context.getMember().getUser().getAvatarUrl());
            builder.setTimestamp(context.getTime());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_daily-already-received",
                    TimeUtils.format(context, context.getUser().getPluginLong(context.getPlugin(), "lastDailyCoins")+86400000 -System.currentTimeMillis())));
            send(context, builder);
            return;
        }
        CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());

        if (lastDailyCoins != -1 && System.currentTimeMillis()-lastDailyCoins > 86400000*2) {
            user.setStreak(0);
        }

        long coins = 200 + context.getUser().getPluginLong(context.getPlugin(), "dailyCoinStreak")*2;
        if (coins > 1000) {
            coins = 1000;
        }

        user.addCoins(coins);
        user.addStreak();
        context.getUser().appendPluginData(context.getPlugin(), "lastDailyCoins", System.currentTimeMillis());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.ORANGE.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                context.getMember().getUser().getAvatarUrl());
        builder.setTimestamp(context.getTime());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_daily-success"));
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_daily-coins"), context.getServer().formatLong(coins), true);
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_daily-streak"), context.getServer().formatLong(context.getUser().getPluginLong(context.getPlugin(), "dailyCoinStreak")), true);
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
