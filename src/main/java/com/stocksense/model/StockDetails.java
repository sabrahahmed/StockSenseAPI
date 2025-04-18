package com.stocksense.model;

public class StockDetails {
    private String symbol;
    private double openPrice;
    private double closePrice;
    private double highPrice;
    private double lowPrice;
    private double volume;
    private double weightedAveragePrice;
    private long timestamp;
    private int numberOfTrades;
    private double dailyChange; // Change between open and close price

    // Getters and setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getOpenPrice() {
        return roundToTwoDecimalPlaces(openPrice);
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = roundToTwoDecimalPlaces(openPrice);
    }

    public double getClosePrice() {
        return roundToTwoDecimalPlaces(closePrice);
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = roundToTwoDecimalPlaces(closePrice);
    }

    public double getHighPrice() {
        return roundToTwoDecimalPlaces(highPrice);
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = roundToTwoDecimalPlaces(highPrice);
    }

    public double getLowPrice() {
        return roundToTwoDecimalPlaces(lowPrice);
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = roundToTwoDecimalPlaces(lowPrice);
    }

    public double getVolume() {
        return roundToTwoDecimalPlaces(volume);
    }

    public void setVolume(double volume) {
        this.volume = roundToTwoDecimalPlaces(volume);
    }

    public double getWeightedAveragePrice() {
        return roundToTwoDecimalPlaces(weightedAveragePrice);
    }

    public void setWeightedAveragePrice(double weightedAveragePrice) {
        this.weightedAveragePrice = roundToTwoDecimalPlaces(weightedAveragePrice);
    }

    public double getDailyChange() {
        return roundToTwoDecimalPlaces(dailyChange);
    }

    public void setDailyChange(double dailyChange) {
        this.dailyChange = roundToTwoDecimalPlaces(dailyChange);
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNumberOfTrades() {
        return numberOfTrades;
    }

    public void setNumberOfTrades(int numberOfTrades) {
        this.numberOfTrades = numberOfTrades;
    }

    // Utility method to round to 2 decimal places
    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}





