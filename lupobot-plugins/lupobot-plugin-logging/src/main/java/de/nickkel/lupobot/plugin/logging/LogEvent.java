package de.nickkel.lupobot.plugin.logging;

import lombok.Getter;

public enum LogEvent {

    MESSAGE_UPDATE("messageUpdate");

    @Getter
    public final String key;

    private LogEvent(String key) {
        this.key = key;
    }

    public String getLocale() {
        return "logging_event-" + this.key.toLowerCase();
    }

    public String getLowKey() {
        return this.key.toLowerCase();
    }
}
