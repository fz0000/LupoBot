package de.nickkel.lupobot.core.pagination;

import de.nickkel.lupobot.core.util.StringUtils;
import lombok.Getter;

import java.util.List;

public class RelatedPages {

    @Getter
    private final List<Page> pages;
    @Getter
    private final String identifier;
    @Getter
    private int currentPage = 0;

    public RelatedPages(List<Page> pages) {
        this.pages = pages;
        this.identifier = StringUtils.getAlphaNumeric(5);
    }

    public void increaseCurrentPage() {
        this.currentPage++;
    }

    public void decreaseCurrentPage() {
        this.currentPage--;
    }
}
