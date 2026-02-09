package com.marketcurrency.api.currency_rate_provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CurrencyRateProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyRateProviderApplication.class, args);
	}

}
