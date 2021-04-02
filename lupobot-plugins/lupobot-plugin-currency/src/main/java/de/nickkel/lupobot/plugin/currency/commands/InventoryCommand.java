package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.pagination.model.Page;
import de.nickkel.lupobot.core.pagination.type.PageType;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.data.Item;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "inventory", category = "items")
public class InventoryCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 2 && context.getArgs()[0].equalsIgnoreCase("upgrade")) {
            long amount = 0;
            try {
                amount = Long.parseLong(context.getArgs()[1]);
            } catch(NumberFormatException e) {
                sendSyntaxError(context, "currency_inventory-upgrade-invalid-amount");
                return;
            }
            if(amount <= 0) {
                sendSyntaxError(context, "currency_inventory-upgrade-too-low-amount");
                return;
            }

            CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
            if(user.getItem(LupoCurrencyPlugin.getInstance().getItem("inventoryslot"))-amount < 0) {
                sendSyntaxError(context, "currency_inventory-upgrade-not-enough", user.getItem(LupoCurrencyPlugin.getInstance().getItem("inventoryslot")));
                return;
            }

            user.addItem(LupoCurrencyPlugin.getInstance().getItem("inventoryslot"), -amount);
            user.addInventorySlots(amount);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_inventory-upgrade",
                    context.getServer().formatLong(amount), context.getServer().formatLong(user.getInventorySlots())));
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            ArrayList<Page> pages = new ArrayList<>();
            CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());

            int i = 0;
            for(Item item : LupoCurrencyPlugin.getInstance().getItems()) {
                if(user.getItem(item) != 0) {
                    i++;
                    if(String.valueOf(i).length() != 1 && (String.valueOf(i).endsWith("1") || i == LupoCurrencyPlugin.getInstance().getItems().size()-1)) {
                        pages.add(new Page(PageType.EMBED, builder.build()));
                        builder.clearFields();
                    }
                    builder.addField(item.getIcon() + " " + user.getItem(item) + "x " + item.getName(), context.getServer().translate(context.getPlugin(), "currency_inventory-price",
                            context.getServer().formatLong(item.getBuy()), context.getServer().formatLong(item.getSell())), false);
                }
            }

            if(user.getUsedInventorySlots() == 0) {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_inventory-empty"));
            } else {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_inventory-inventoryslots",
                        user.getUsedInventorySlots(), context.getServer().formatLong(user.getInventorySlots())));
            }

            if(pages.size() != 0) {
                context.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                    Pages.paginate(success, pages, 60, TimeUnit.SECONDS, reactUser -> context.getUser().getId() == reactUser.getIdLong());
                });
            } else {
                context.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }
}