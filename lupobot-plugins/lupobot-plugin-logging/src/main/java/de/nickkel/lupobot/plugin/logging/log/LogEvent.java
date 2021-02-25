package de.nickkel.lupobot.plugin.logging.log;

import lombok.Getter;

public enum LogEvent {

    MESSAGE_UPDATE("messageUpdate"),
    MESSAGE_DELETE("messageDelete"),
    NICKNAME_UPDATE("nicknameUpdate"),
    ONLINESTATUS_UPDATE("onlineStatusUpdate"),
    ACTIVITY_UPDATE("activityUpdate"),
    ROLE_ADD("roleAdd"),
    ROLE_REMOVE("roleRemove"),
    MEMBER_JOIN("memberJoin"),
    MEMBER_LEAVE("memberLeave");

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
