package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.entities.Item;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandInfo(name = "itemchest", category = "chest")
public class ItemChestCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
        if (user.getItem(LupoCurrencyPlugin.getInstance().getItem("itemchest")) == 0) {
            sendSyntaxError(context, "currency_itemchest-buy-item");
            return;
        }
        user.addItem(LupoCurrencyPlugin.getInstance().getItem("itemchest"), -1);
        Item item1 = user.getRandomItem(65, 33, 2), item2 = user.getRandomItem(75, 23, 2);
        user.addItem(item1, 1);
        user.addItem(item2, 1);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setColor(LupoColor.GREEN.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_itemchest-success", item1.getIcon() + " " + item1.getName(),
                item1.getBuy(), item2.getIcon() + " " + item2.getName(), item2.getBuy()));
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
