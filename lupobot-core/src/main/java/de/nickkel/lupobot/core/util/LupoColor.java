package de.nickkel.lupobot.core.util;

import lombok.Getter;

import java.awt.*;

public enum LupoColor {

    RED("#DE3A18"),
    GREEN("#2EDC23"),
    BLUE("#0066CC"),
    YELLOW("#F5E40E"),
    DARK_GRAY("#4a4a4a"),
    AQUA("#0DD5B4"),
    PURPLE("#B737E7"),
    PINK("#EB3CD8"),
    ORANGE("#F5A614");


    @Getter
    private final String hex;
    @Getter
    private final Color color;

    private LupoColor(String hex) {
        this.hex = hex;
        this.color = Color.decode(hex);
    }
}
