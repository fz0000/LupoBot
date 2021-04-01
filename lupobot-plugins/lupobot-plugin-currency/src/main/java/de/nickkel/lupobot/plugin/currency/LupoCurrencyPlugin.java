package de.nickkel.lupobot.plugin.currency;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Map;

@PluginInfo(name = "currency", version = "1.0.0", author = "Nickkel")
public class LupoCurrencyPlugin extends LupoPlugin {

    @Getter
    private static LupoCurrencyPlugin instance;
    private final Map<Long, CurrencyUser> currencyUser = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        LupoBot.getInstance().getCommandHandler().registerCommands(this, "de.nickkel.lupobot.plugin.currency.commands");
    }

    @Override
    public void onDisable() {

    }

    public CurrencyUser getCurrencyUser(Member member) {
        if(!this.currencyUser.containsKey(member.getIdLong())) {
            this.currencyUser.put(member.getIdLong(), new CurrencyUser(member));
        }
        return this.currencyUser.get(member.getIdLong());
    }
}
