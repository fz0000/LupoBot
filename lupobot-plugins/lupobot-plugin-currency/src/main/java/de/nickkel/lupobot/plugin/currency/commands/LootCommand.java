package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.data.Item;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

@CommandInfo(name = "loot", category = "reward", cooldown = 600)
public class LootCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
        if(user.getUsedInventorySlots()+1 > user.getInventorySlots()) {
            sendSyntaxError(context, "currency_loot-no-inventory-space");
            return;
        }

        int randomCoins = new Random().nextInt(100);
        user.addCoins(randomCoins);
        Item item = user.getRandomItem(90, 9, 1);
        user.addItem(item, 1);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setColor(LupoColor.GREEN.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_loot-success",
                context.getServer().formatLong(Long.parseLong(String.valueOf(randomCoins))), item.getIcon() + " " + item.getName(), context.getServer().formatLong(item.getBuy())));

        context.getChannel().sendMessage(builder.build()).queue();
    }
}