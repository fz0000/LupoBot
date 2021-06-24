package de.nickkel.lupobot.core.util;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

public class StaffGroup {

    @Getter
    private final long id;

    public StaffGroup(Role role) {
        if (role != null) {
            this.id = role.getIdLong();
        } else {
            this.id = -1;
        }
    }

    public long getPower() {
        if (this.id == -1) {
            return 0L;
        }

        Document groups = new Document(LupoBot.getInstance().getConfig().getJsonElement("staffGroups").getAsJsonObject());
        if (groups.has(String.valueOf(this.id))) {
            return groups.getLong(String.valueOf(this.id));
        } else {
            return 0L;
        }
    }
}
