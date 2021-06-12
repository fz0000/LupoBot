package de.nickkel.lupobot.core.pagination;

import de.nickkel.lupobot.core.data.LupoServer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Paginator {

    @Getter
    private static Map<UUID, Page> pages = new HashMap<>();
    @Getter
    private static Map<String, RelatedPages> relatedPages = new HashMap<>();
    public static final String PREFIX = "PAGINATOR";

    public static void paginate(TextChannel channel, List<Page> pageList, long timeout) {
        RelatedPages pages = new RelatedPages(pageList);
        LupoServer server = LupoServer.getByGuild(channel.getGuild());
        Button last = Button.secondary(PREFIX + ";LAST;" + pages.getIdentifier(), server.translate(null, "core_pagination-last")).withEmoji(Emoji.fromMarkdown("◀"));
        Button next = Button.secondary(PREFIX + ";NEXT;" + pages.getIdentifier(), server.translate(null, "core_pagination-next")).withEmoji(Emoji.fromMarkdown("▶"));
        Paginator.relatedPages.put(pages.getIdentifier(), pages);

        if (timeout == 0) {
            channel.sendMessage(pages.getPages().get(0).getEmbed()).setActionRow(last, next).queue();
            return;
        }

        channel.sendMessage(pages.getPages().get(0).getEmbed()).setActionRow(last, next)
                .delay(timeout, TimeUnit.SECONDS)
                .flatMap((it) -> it.editMessage(it).setActionRow(last.asDisabled(), next.asDisabled()))
                .queue();
    }

    public static void paginate(Message message, List<Page> pageList, long timeout) {
        RelatedPages pages = new RelatedPages(pageList);
        LupoServer server = LupoServer.getByGuild(message.getGuild());
        Button last = Button.secondary(PREFIX + ";LAST;" + pages.getIdentifier(), server.translate(null, "core_pagination-last")).withEmoji(Emoji.fromMarkdown("◀"));
        Button next = Button.secondary(PREFIX + ";NEXT;" + pages.getIdentifier(), server.translate(null, "core_pagination-next")).withEmoji(Emoji.fromMarkdown("▶"));
        Paginator.relatedPages.put(pages.getIdentifier(), pages);
        message.editMessage(pages.getPages().get(0).getEmbed()).setActionRow(last, next).queue();

        if (timeout == 0) {
            message.editMessage(pages.getPages().get(0).getEmbed()).setActionRow(last, next).queue();
        }

        message.editMessage(pages.getPages().get(0).getEmbed()).setActionRow(last, next)
                .delay(timeout, TimeUnit.SECONDS)
                .flatMap((it) -> it.editMessage(it).setActionRow(last.asDisabled(), next.asDisabled()))
                .queue();
    }

    public static void categorize(Message message, List<Page> pages, long timeout) {
        List<Button> buttons = new ArrayList<>();
        for (Page page : pages) {
            page.setButton(page.getButton().withId(PREFIX + ";" + page.getUuid()));
            buttons.add(page.getButton());
        }

        if (timeout == 0) {
            message.editMessage(message).setActionRow(buttons).queue();
        }

        List<Button> disabledButtons = buttons.stream().map(Button::asDisabled).collect(Collectors.toList());
        message.editMessage(message).setActionRow(buttons)
                .delay(timeout, TimeUnit.SECONDS)
                .flatMap((it) -> it.editMessage(it).setActionRow(disabledButtons))
                .queue();
    }

    public static void categorize(TextChannel channel, List<Page> pages, long timeout) {
        List<Button> buttons = new ArrayList<>();
        for (Page page : pages) {
            page.setButton(page.getButton().withId(PREFIX + ";" + page.getUuid()));
            buttons.add(page.getButton());
        }

        if (timeout == 0) {
            channel.sendMessage(pages.get(0).getEmbed()).setActionRow(buttons).queue();
        }

        List<Button> disabledButtons = buttons.stream().map(Button::asDisabled).collect(Collectors.toList());
        channel.sendMessage(pages.get(0).getEmbed()).setActionRow(buttons)
                .delay(timeout, TimeUnit.SECONDS)
                .flatMap((it) -> it.editMessage(it).setActionRow(disabledButtons))
                .queue();
    }
}
