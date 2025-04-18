package com.stocksense.dto.youtube;

public class YouTubeItemDto {
    private YouTubeVideoIdDto id;
    private YouTubeSnippetDto snippet;

    // Getters and Setters
    public YouTubeVideoIdDto getId() {
        return id;
    }

    public void setId(YouTubeVideoIdDto id) {
        this.id = id;
    }

    public YouTubeSnippetDto getSnippet() {
        return snippet;
    }

    public void setSnippet(YouTubeSnippetDto snippet) {
        this.snippet = snippet;
    }
}
