package de.nickkel.lupobot.plugin.profile.enums;

public enum Gender {
    MALE, FEMALE, DIVERSE;

    public String getLocale() {
        return "profile_gender-" + this.name().toLowerCase();
    }
}
