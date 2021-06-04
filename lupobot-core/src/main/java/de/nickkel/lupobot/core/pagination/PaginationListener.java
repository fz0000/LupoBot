package de.nickkel.lupobot.core.pagination;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Component;

import java.util.UUID;

public class PaginationListener extends ListenerAdapter {

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        if (event.getComponentType() == Component.Type.BUTTON && event.getComponentId().startsWith(Paginator.PREFIX) &&
            event.getMessage() != null && event.getMember() != null && event.getMessage().getAuthor().getIdLong() == LupoBot.getInstance().getSelfUser().getIdLong()) {
            LupoServer server = LupoServer.getByGuild(event.getGuild());
            String[] id = event.getComponentId().split(";");

            // paginate
            if (id.length == 3) {
                if (Paginator.getRelatedPages().containsKey(id[2]) && (id[1].equals("LAST") || id[1].equals("NEXT"))) {
                    String type = id[1];
                    String identifier = id[2];

                    RelatedPages pages = Paginator.getRelatedPages().get(identifier);

                    if (pages.getPages().get(pages.getCurrentPage()).getWhitelist().size() == 0 || pages.getPages().get(pages.getCurrentPage()).getWhitelist().contains(event.getMember().getIdLong())) {
                        if (type.equals("NEXT") && pages.getCurrentPage()+1 < pages.getPages().size()) {
                            event.getMessage().editMessage(pages.getPages().get(pages.getCurrentPage()+1).getBuilder().build()).queue();
                            pages.increaseCurrentPage();
                        } else if(type.equals("LAST") && pages.getCurrentPage() != 0) {
                            event.getMessage().editMessage(pages.getPages().get(pages.getCurrentPage()-1).getBuilder().build()).queue();
                            pages.decreaseCurrentPage();
                        }
                        event.deferEdit().queue();
                    } else {
                        event.reply(server.translate(null, "core_pagination-no-permission")).setEphemeral(true).queue();
                    }
                }

            // categorize
            } else if(event.getComponentId().contains("-")) {
                String uuid = id[1];
                if (Paginator.getPages().containsKey(UUID.fromString(uuid))) {
                    Page page = Paginator.getPages().get(UUID.fromString(uuid));
                    event.deferEdit().queue();
                    if (page.getBuilder() != null && (page.getWhitelist().size() == 0 || page.getWhitelist().contains(event.getMember().getIdLong()))) {
                        event.getMessage().editMessage(page.getBuilder().build()).queue();
                    }
                    if (page.getConsumer() != null) {
                        page.getConsumer().accept(event.getMember(), event.getMessage());
                    }
                }
            }
        }
    }
}
