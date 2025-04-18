package com.stocksense.dto;

public class FavoriteStockDTO {
    private String symbol;
    private Double closePrice;
    private Double dailyChange;

    // Constructor
    public FavoriteStockDTO(String symbol, Double closePrice, Double dailyChange) {
        this.symbol = symbol;
        this.closePrice = closePrice;
        this.dailyChange = dailyChange;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Double getDailyChange() {
        return dailyChange;
    }

    public void setDailyChange(Double dailyChange) {
        this.dailyChange = dailyChange;
    }
}
