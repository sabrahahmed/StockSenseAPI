package com.stocksense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class PolygonStockData {

    @JsonProperty("T") private String symbol;
    @JsonProperty("v") private JsonNode volume;
    @JsonProperty("vw") private JsonNode weightedAveragePrice;
    @JsonProperty("o") private JsonNode openPrice;
    @JsonProperty("c") private JsonNode closePrice;
    @JsonProperty("h") private JsonNode highPrice;
    @JsonProperty("l") private JsonNode lowPrice;
    @JsonProperty("t") private long timestamp;
    @JsonProperty("n") private int numberOfTrades;

    // Getters and safe conversion methods
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getVolume() { return volume != null && !volume.isNull() ? volume.asDouble() : 0.0; }
    public void setVolume(JsonNode volume) { this.volume = volume; }

    public double getWeightedAveragePrice() { return weightedAveragePrice != null && !weightedAveragePrice.isNull() ? weightedAveragePrice.asDouble() : 0.0; }
    public void setWeightedAveragePrice(JsonNode weightedAveragePrice) { this.weightedAveragePrice = weightedAveragePrice; }

    public double getOpenPrice() { return openPrice != null && !openPrice.isNull() ? openPrice.asDouble() : 0.0; }
    public void setOpenPrice(JsonNode openPrice) { this.openPrice = openPrice; }

    public double getClosePrice() { return closePrice != null && !closePrice.isNull() ? closePrice.asDouble() : 0.0; }
    public void setClosePrice(JsonNode closePrice) { this.closePrice = closePrice; }

    public double getHighPrice() { return highPrice != null && !highPrice.isNull() ? highPrice.asDouble() : 0.0; }
    public void setHighPrice(JsonNode highPrice) { this.highPrice = highPrice; }

    public double getLowPrice() { return lowPrice != null && !lowPrice.isNull() ? lowPrice.asDouble() : 0.0; }
    public void setLowPrice(JsonNode lowPrice) { this.lowPrice = lowPrice; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getNumberOfTrades() { return numberOfTrades; }
    public void setNumberOfTrades(int numberOfTrades) { this.numberOfTrades = numberOfTrades; }
}
