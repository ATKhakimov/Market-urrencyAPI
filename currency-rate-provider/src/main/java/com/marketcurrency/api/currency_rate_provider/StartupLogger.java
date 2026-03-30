package com.marketcurrency.api.currency_rate_provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {
	private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

	private final ObjectProvider<BuildProperties> buildProperties;

	public StartupLogger(ObjectProvider<BuildProperties> buildProperties) {
		this.buildProperties = buildProperties;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		BuildProperties props = buildProperties.getIfAvailable();
		String name = props != null ? props.getName() : "currency-rate-provider";
		String version = props != null ? props.getVersion() : "unknown";
		logger.info("Service started: name={} version={}", name, version);
	}
}
