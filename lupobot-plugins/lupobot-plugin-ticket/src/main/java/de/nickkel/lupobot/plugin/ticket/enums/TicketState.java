package de.nickkel.lupobot.plugin.ticket.enums;

import lombok.Getter;

public enum TicketState {
    OPENED("openedCategory"), ASSIGNED("assignedCategory"), CLOSED("closedCategory");

    @Getter
    public final String key;

    private TicketState(String key) {
        this.key = key;
    }
}
