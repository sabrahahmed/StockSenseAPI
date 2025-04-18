package com.stocksense.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockAbout {

    public StockAbout() {
    }
    @Id
    @Column(name = "stock_symbol", length = 10)
    @JsonProperty("stockSymbol")
    private String stockSymbol;

    @Column(name = "name", nullable = false, length = 255)
    @JsonProperty("name")
    private String name;

    @Column(name = "sector", length = 100)
    @JsonProperty("sector")
    private String sector;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "market_cap")
    @JsonProperty("market_cap")
    private Long marketCap;

    @Column(name = "ipo_date")
    @JsonProperty("ipo_date")
    private String ipoDate;

    @Column(name = "exchange", length = 50)
    @JsonProperty("exchange")
    private String exchange;

    @Column(name = "country", length = 50)
    @JsonProperty("country")
    private String country;

    @Column(name = "ceo", length = 255)
    @JsonProperty("ceo")
    private String ceo;

    @Column(name = "employees")
    @JsonProperty("employees")
    private Integer employees;

    @Column(name = "website", length = 255)
    @JsonProperty("website")
    private String website;

    @Column(name = "currency", length = 10)
    @JsonProperty("currency")
    private String currency;

    @Column(name = "logo_url", length = 255)
    @JsonProperty("logo_url")
    private String logoUrl;

    @Column(name = "headquarters", length = 255)
    @JsonProperty("headquarters")
    private String headquarters;

    @Column(name = "industry", length = 100)
    @JsonProperty("industry")
    private String industry;
}
