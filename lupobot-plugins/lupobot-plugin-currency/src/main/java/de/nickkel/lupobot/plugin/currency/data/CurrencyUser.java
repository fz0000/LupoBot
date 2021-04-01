package de.nickkel.lupobot.plugin.currency.data;

import com.mongodb.BasicDBList;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyUser {

    @Getter
    private final LupoUser user;
    @Getter
    private final LupoPlugin plugin;

    public CurrencyUser(Member member) {
        this.plugin = LupoBot.getInstance().getPlugin("currency");
        this.user = LupoUser.getByMember(member);
    }

    public long getCoins() {
        return (long) (int) this.user.getPluginData(this.plugin, "coins");
    }

    public void addCoins(long amount) {
        this.user.appendPluginData(this.plugin, "coins", (long) this.user.getPluginData(this.plugin, "coins")+amount);
    }
}
