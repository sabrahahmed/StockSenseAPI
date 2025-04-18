package com.stocksense.service;

import com.stocksense.model.StockDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class PriceService {
    private static final String POLYGON_API_KEY = "FnYRNumODlY2FjP_yaSer0jR7yCWSH_H";
    private static final String POLYGON_API_URL = "https://api.polygon.io/v2/aggs/ticker/%s/prev?apiKey=" + POLYGON_API_KEY;

    public StockDetails getStockPrice(String stockSymbol) {
        // Format the URL by inserting the stockId into the URL template
        String url = String.format(POLYGON_API_URL, stockSymbol);

        // Set up RestTemplate to make the API call
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Fetch the response as a Map and directly map to the results array
            Map<String, Object> apiResponse = restTemplate.getForObject(url, Map.class);

            if (apiResponse != null && apiResponse.get("results") != null) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) apiResponse.get("results");

                // Convert the first result to StockPrice
                Map<String, Object> result = results.get(0);

                StockDetails stockDetails = new StockDetails();
                stockDetails.setSymbol((String) result.get("T"));
                stockDetails.setOpenPrice(convertToDouble(result.get("o")));
                stockDetails.setClosePrice(convertToDouble(result.get("c")));
                stockDetails.setHighPrice(convertToDouble(result.get("h")));
                stockDetails.setLowPrice(convertToDouble(result.get("l")));
                stockDetails.setVolume(convertToDouble(result.get("v")));
                stockDetails.setWeightedAveragePrice(convertToDouble(result.get("vw")));
                stockDetails.setTimestamp((Long) result.get("t"));
                stockDetails.setNumberOfTrades((Integer) result.get("n"));

                // Calculate daily change (close - open)
                double dailyChange = stockDetails.getClosePrice() - stockDetails.getOpenPrice();
                stockDetails.setDailyChange(dailyChange);

                return stockDetails;  // Return the populated StockPrice object
            }
        } catch (Exception e) {
            // Handle error (e.g., log the error)
            System.out.println("Error fetching stock data: " + e.getMessage());
        }
        return null;  // Return null if error occurs or no data found
    }

    // Utility method to safely convert values to Double
    private Double convertToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;  // Default value if null or not a number
    }

}
