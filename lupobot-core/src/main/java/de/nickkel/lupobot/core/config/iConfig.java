package de.nickkel.lupobot.core.config;

import com.google.gson.JsonElement;

import java.util.List;

public interface iConfig {

    Config append(String key, String value);

    Config append(String key, Number value);

    Config append(String key, Integer value);

    Config append(String key, Boolean value);

    Config append(String key, Long value);

    Config append(String key, Object value);

    Config append(String key, List<String> value);

    Config append(String key, JsonElement value);

}