package com.stocksense.dto.youtube;

import java.util.List;

public class YouTubeSearchResponseDto {
    private List<YouTubeItemDto> items;

    // Getters and Setters
    public List<YouTubeItemDto> getItems() {
        return items;
    }

    public void setItems(List<YouTubeItemDto> items) {
        this.items = items;
    }
}
