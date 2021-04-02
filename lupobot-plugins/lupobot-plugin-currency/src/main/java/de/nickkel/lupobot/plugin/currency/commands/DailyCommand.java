package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@CommandInfo(name = "daily", category = "reward")
public class DailyCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        long lastDailyCoins = context.getUser().getPluginLong(context.getPlugin(), "lastDailyCoins");
        if(lastDailyCoins != -1 && lastDailyCoins+86400000-System.currentTimeMillis() > 0) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.RED.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                    context.getMember().getUser().getAvatarUrl());
            builder.setTimestamp(context.getMessage().getTimeCreated());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_daily-already-received",
                    TimeUtils.format(context, context.getUser().getPluginLong(context.getPlugin(), "lastDailyCoins")+86400000 -System.currentTimeMillis())));
            context.getChannel().sendMessage(builder.build()).queue();
            return;
        }

        long coins = 200 + context.getUser().getPluginLong(context.getPlugin(), "dailyCoinStreak")*2;
        if(coins > 1000) {
            coins = 1000;
        }

        CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
        user.addCoins(coins);
        user.addStreak();
        context.getUser().appendPluginData(context.getPlugin(), "lastDailyCoins", System.currentTimeMillis());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.ORANGE.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                context.getMember().getUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_daily-success"));
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_daily-coins"), context.getServer().formatLong(coins), true);
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_daily-streak"), context.getServer().formatLong(context.getUser().getPluginLong(context.getPlugin(), "dailyCoinStreak")), true);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
