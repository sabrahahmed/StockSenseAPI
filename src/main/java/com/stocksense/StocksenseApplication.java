package com.stocksense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.stocksense", exclude = {SecurityAutoConfiguration.class}) // TODO
@EnableJpaRepositories(basePackages = "com.stocksense.repository")
@EnableCaching
public class StocksenseApplication {

	public static void main(String[] args) {
		SpringApplication.run(StocksenseApplication.class, args);
	}
}

