package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

@CommandInfo(name = "coinchest", category = "chest", cooldown = 3)
public class CoinChestCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
        if (user.getItem(LupoCurrencyPlugin.getInstance().getItem("coinchest")) == 0) {
            sendSyntaxError(context, "currency_coinchest-buy-item");
            return;
        }

        user.addItem(LupoCurrencyPlugin.getInstance().getItem("coinchest"), -1);
        long coins;
        int percent = new Random().nextInt(100);

        if (percent < 80){
            coins = new Random().nextInt((800-300)+1) + 300;
        } else if (percent < 90){
            coins = new Random().nextInt((2000-800)+1) + 800;
        } else {
            coins = new Random().nextInt((3000-2000)+1) + 2000;
        }
        user.addCoins(coins);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setColor(LupoColor.GREEN.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_coinchest-success", context.getServer().formatLong(coins)));
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
