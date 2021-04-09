package de.nickkel.lupobot.plugin.profile.data;

import com.mongodb.BasicDBList;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.plugin.profile.enums.Badge;
import de.nickkel.lupobot.plugin.profile.enums.Gender;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Profile {

    @Getter
    private final LupoUser user;
    @Getter
    private final LupoPlugin plugin;

    public Profile(Member member) {
        this.plugin = LupoBot.getInstance().getPlugin("profile");
        this.user = LupoUser.getByMember(member);
    }

    public List<Badge> getBadges() {
        BasicDBList dbList = (BasicDBList) this.user.getPluginData(this.plugin, "badges");
        List<Badge> badges = new ArrayList<>();
        for (Object name : dbList) {
            badges.add(Badge.valueOf((String) name));
        }
        return badges;
    }

    public void addBadge(Badge badge) {
        BasicDBList dbList = (BasicDBList) this.user.getPluginData(this.plugin, "badges");
        if (!dbList.contains(badge.toString())) {
            dbList.add(badge.toString());
            this.user.appendPluginData(this.plugin, "badges", dbList);
        }
    }

    public void removeBadge(Badge badge) {
        BasicDBList dbList = (BasicDBList) this.user.getPluginData(this.plugin, "badges");
        dbList.remove(badge.toString());
        this.user.appendPluginData(this.plugin, "badges", dbList);
    }

    public String getStatus() {
        String status = (String) this.user.getPluginData(this.plugin, "status");
        return (String) this.user.getPluginData(this.plugin, "status");
    }

    public void setStatus(String status) {
        this.user.appendPluginData(this.plugin, "status", status);
    }

    public Gender getGender() {
        String gender = (String) this.user.getPluginData(this.plugin, "gender");
        if (gender != null) {
            return Gender.valueOf(gender);
        }
        return null;
    }

    public void setGender(Gender gender) {
        this.user.appendPluginData(this.plugin, "gender", gender.toString());
    }

    public String getBirthday() {
        return (String) this.user.getPluginData(this.plugin, "birthday");
    }

    public void setBirthday(String date) {
        if (isValidDate(date)) {
          this.user.appendPluginData(this.plugin, "birthday", date);
        }
    }

    public int getAge() {
        LocalDate birthday = LocalDate.parse(getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Period period = Period.between(birthday, LocalDate.now());
        return period.getYears();
    }

    public boolean isValidDate(String date) {
        boolean valid = false;
        try {
            if (date.split("-").length == 3) {
                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                date = "1000-" + date;
            }
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            valid = true;
        } catch (DateTimeParseException ignored) {
        }

        return valid;
    }
}
