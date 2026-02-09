package com.marketcurrency.api.rate_printer.scheduler;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.marketcurrency.api.rate_printer.client.RateClient;
import com.marketcurrency.api.rate_printer.client.RateClientException;
import com.marketcurrency.api.rate_printer.dto.GetRateResult;

@Component
public class PollingJob {
	private static final Logger logger = LoggerFactory.getLogger(PollingJob.class);
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

	private final RateClient rateClient;

	public PollingJob(RateClient rateClient) {
		this.rateClient = rateClient;
	}

	@Scheduled(fixedDelay = 5000)
	public void poll() {
		try {
			GetRateResult result = rateClient.getRate("USDRUB");
			String timestamp = FORMATTER.format(Instant.ofEpochMilli(result.getTimestampMs()).atOffset(ZoneOffset.UTC));
			logger.info("{} {}={}", timestamp, result.getPair(), result.getRate());
		} catch (RateClientException ex) {
			logger.warn("Failed to fetch rate: {}", ex.getMessage());
		}
	}
}
