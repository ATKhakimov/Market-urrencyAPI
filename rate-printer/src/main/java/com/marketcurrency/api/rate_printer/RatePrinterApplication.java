package com.marketcurrency.api.rate_printer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class RatePrinterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatePrinterApplication.class, args);
	}

}
