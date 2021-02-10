package de.nickkel.lupobot.core.language;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.util.FileResourcesUtils;
import lombok.Getter;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

public class LanguageHandler {

    @Getter
    private final Map<String, Language> languages = new HashMap<>();
    @Getter
    private final String FALLBACK = "en_US";

    public LanguageHandler(Class clazz) {
        FileResourcesUtils app = new FileResourcesUtils(clazz);
        try {
            List<Path> result = app.getPathsFromResourceJAR("locales");
            for (Path path : result) {
                String filePathInJAR = path.toString();

                // Windows will returns /json/file1.json, cut the first /
                // the correct path should be json/file1.json
                if (filePathInJAR.startsWith("/")) {
                    filePathInJAR = filePathInJAR.substring(1, filePathInJAR.length());
                }

                InputStream is = app.getFileFromResourceAsStream(filePathInJAR);
                Properties properties = app.getPropertiesByInputStream(is);
                loadLanguage(filePathInJAR.split("/")[1].replace(".properties", ""), properties);
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLanguage(String language, Properties properties) {
        final Map<String, String> translations = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            translations.put(key, properties.getProperty(key));
        }
        languages.put(language, new Language(language, translations));
        if(!LupoBot.getInstance().getAvailableLanguages().contains(language)) {
            LupoBot.getInstance().getAvailableLanguages().add(language);
        }
        LupoBot.getInstance().getLogger().info("Loaded language " + language + " with " + translations.size() + " strings");
    }

    public String translate(String language, String key, Object... params) {
        try {
            return languages.getOrDefault(language, languages.get(this.FALLBACK)).translate(key, params);
        } catch (NullPointerException e) {
            if(languages.get(this.FALLBACK).getTranslations().containsKey(key)) {
                return languages.get(this.FALLBACK).translate(key, params);
            }
            return "N/A (" + key + ")";
        }
    }

    public int maximum(String language, String locale) {
        int max = 0;
        for(int i = 1; !this.translate(language, locale + "_" + i ).startsWith("N/A"); max = i++) {
        }
        return max;
    }

    public String getRandomTranslation(String language, String locale, Object... params) {
        int random = new Random().nextInt(maximum(language, locale))+1;
        return translate(language, locale + "_" + random, params);
    }
}