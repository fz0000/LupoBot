package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.*;
import de.nickkel.lupobot.core.pagination.Page;
import de.nickkel.lupobot.core.pagination.Paginator;
import de.nickkel.lupobot.core.pagination.RelatedPages;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.entities.Item;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "inventory", aliases = "inv", category = "items")
@SlashSubCommand(name = "upgrade", options = {@SlashOption(name = "amount", type = OptionType.INTEGER, required = false)})
@SlashSubCommand(name = "see")
public class InventoryCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (((context.getArgs().length == 1 || context.getArgs().length == 2) && context.getArgs()[0].equalsIgnoreCase("upgrade")) ||
                context.getSlash() != null && context.getSlash().getSubcommandName().equalsIgnoreCase("upgrade")) {
            long amount;
            if (context.getArgs().length == 1 || (context.getSlash() != null && context.getSlash().getOption("amount") == null)) {
                amount = 1;
            } else {
                try {
                    if (context.getSlash() == null) {
                        amount = Long.parseLong(context.getArgs()[1]);
                    } else {
                        amount = context.getSlash().getOption("amount").getAsLong();
                    }
                } catch (NumberFormatException e) {
                    sendSyntaxError(context, "currency_inventory-upgrade-invalid-amount");
                    return;
                }
            }
            if (amount <= 0) {
                sendSyntaxError(context, "currency_inventory-upgrade-too-low-amount");
                return;
            }

            CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
            if (user.getItem(LupoCurrencyPlugin.getInstance().getItem("inventoryslot"))-amount < 0) {
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
            send(context, builder);
        } else {
            ArrayList<Page> pages = new ArrayList<>();
            CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());

            if (user.getUsedInventorySlots() == 0) {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_inventory-empty"));
            } else {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_inventory-inventoryslots",
                        user.getUsedInventorySlots(), context.getServer().formatLong(user.getInventorySlots())));
            }

            int max = 0;
            for (Item item : LupoCurrencyPlugin.getInstance().getItems()) {
                if (user.getItem(item) != 0) {
                    max++;
                }
            }

            int i = 0;
            for (Item item : LupoCurrencyPlugin.getInstance().getItems()) {
                if (user.getItem(item) != 0) {
                    builder.addField(item.getIcon() + " " + user.getItem(item) + "x " + item.getName(), context.getServer().translate(context.getPlugin(), "currency_inventory-price",
                            context.getServer().formatLong(item.getBuy()), context.getServer().formatLong(item.getSell())), false);
                    if (String.valueOf(i).length() != 1 && (String.valueOf(i).endsWith("0") || i == max-1)) {
                        Page page = new Page(builder.build());
                        page.getWhitelist().add(context.getMember().getIdLong());
                        pages.add(page);
                        builder.clearFields();
                    }
                    i++;
                }
            }

            if (pages.size() != 0) {
                Paginator.paginate(context, pages, 60);
            } else {
                send(context, builder);
            }
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}