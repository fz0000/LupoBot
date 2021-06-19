package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.Instant;

@CommandInfo(name = "richest", category = "general")
public class RichestCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String userNames = "", coins = "";
        int rank = 1;
        for (Long id : LupoCurrencyPlugin.getInstance().getRichestList().getSortedUsers().keySet()) {
            userNames = userNames + rank + ". " + LupoCurrencyPlugin.getInstance().getRichestList().getUsersAsMention().get(id) + "\n";
            coins = coins + context.getServer().formatLong(LupoCurrencyPlugin.getInstance().getRichestList().getSortedUsers().get(id)) + "\n";
            rank++;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setFooter(context.getServer().translate(context.getPlugin(), "currency_richest-last-refresh"));
        builder.setTimestamp(Instant.ofEpochMilli(LupoCurrencyPlugin.getInstance().getRichestList().getLastRefresh()));
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(context.getServer().translate(context.getPlugin(), "currency_richest-title"), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_richest-name"), userNames, true);
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_richest-coins"), coins, true);
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}