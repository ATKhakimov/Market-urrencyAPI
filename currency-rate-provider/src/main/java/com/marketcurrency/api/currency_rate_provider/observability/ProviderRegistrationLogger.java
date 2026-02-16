package com.marketcurrency.api.currency_rate_provider.observability;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProviderRegistrationLogger {
	private static final Logger logger = LoggerFactory.getLogger(ProviderRegistrationLogger.class);

	private final DiscoveryClient discoveryClient;
	private final Environment environment;

	public ProviderRegistrationLogger(DiscoveryClient discoveryClient, Environment environment) {
		this.discoveryClient = discoveryClient;
		this.environment = environment;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		String serviceName = environment.getProperty("spring.application.name", "currency-rate-provider");
		List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
		logger.info("Provider registration in ZooKeeper: serviceId={}, localPort={}, discoveredInstances={}.",
				serviceName,
			environment.getProperty("local.server.port", "unknown"),
				instances.size());
	}
}
