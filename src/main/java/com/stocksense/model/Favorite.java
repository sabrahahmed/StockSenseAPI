package com.stocksense.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    // Constructor to initialize the Favorite with a User and a stockSymbol
    public Favorite() {
    }

    public Favorite(User user, String stockSymbol) {
        this.userId = user.getId(); // Assuming User class has a getId() method to fetch user ID
        this.stockSymbol = stockSymbol;
    }

    @Id
    @Column(name = "id", length = 10)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @Column(name = "user_id", nullable = false, length = 255)
    @JsonProperty("userId")
    private Long userId; // This stores the ID of the User

    @Column(name = "stock_symbol", nullable = false, length = 10)
    @JsonProperty("stock_symbol")
    private String stockSymbol; // This stores the stock symbol

    // Explicit Getter if needed (Lombok should handle this automatically with @Getter)
    public String getStockSymbol() {
        return stockSymbol;
    }
}
