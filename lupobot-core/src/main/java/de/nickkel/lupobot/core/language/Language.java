package de.nickkel.lupobot.core.language;

import lombok.Getter;

import java.text.MessageFormat;
import java.util.Map;

public class Language {

    @Getter
    private final String name;
    @Getter
    private final Map<String, String> translations;

    public Language(String name, Map<String, String> translations) {
        this.name = name;
        this.translations = translations;
    }

    public String translate(String key, Object... params) {
        String value;
        try {
            value = translations.get(key);
        } catch (NullPointerException e) {
            value = "N/A (" + key + ")";
        }
        return MessageFormat.format(value, params);
    }
}