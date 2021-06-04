package de.nickkel.lupobot.core.pagination;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.Button;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class Page {

    @Getter
    private EmbedBuilder builder;
    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter @Setter
    private Button button;
    @Getter @Setter
    private BiConsumer<Member, Message> consumer;
    @Getter
    private List<Long> whitelist = new ArrayList<>();

    public Page(EmbedBuilder builder) {
        this.builder = builder;
        Paginator.getPages().put(this.uuid, this);
    }

    public Page(Button button, BiConsumer<Member, Message> consumer) {
        this.button = button;
        this.consumer = consumer;
        Paginator.getPages().put(this.uuid, this);
    }

    public Page(Button button, EmbedBuilder builder) {
        this.button = button;
        this.builder = builder;
        Paginator.getPages().put(this.uuid, this);
    }

    public Page(Button button, EmbedBuilder builder, BiConsumer<Member, Message> consumer) {
        this.button = button;
        this.builder = builder;
        this.consumer = consumer;
        Paginator.getPages().put(this.uuid, this);
    }

    public Page(Button button, EmbedBuilder builder, BiConsumer<Member, Message> consumer, List<Long> whitelist) {
        this.builder = builder;
        this.button = button;
        this.consumer = consumer;
        this.whitelist = whitelist;
        Paginator.getPages().put(this.uuid, this);
    }
}
