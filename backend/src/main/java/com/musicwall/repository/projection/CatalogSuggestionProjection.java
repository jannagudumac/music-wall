package com.musicwall.repository.projection;

// Spring Data reads the SQL result through these getters; the score helps the service rank
// suggestions.
public interface CatalogSuggestionProjection {

    Long getId();

    String getType();

    String getTitle();

    String getSubtitle();

    Float getScore();
}
