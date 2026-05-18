package com.marketcurrency.api.currency_rate_provider.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class PreShutdownDeregistration implements SmartLifecycle {
	private static final Logger logger = LoggerFactory.getLogger(PreShutdownDeregistration.class);

	private final ObjectProvider<ServiceRegistry> serviceRegistry;
	private final ObjectProvider<Registration> registration;
	private volatile boolean running;

	public PreShutdownDeregistration(ObjectProvider<ServiceRegistry> serviceRegistry,
			ObjectProvider<Registration> registration) {
		this.serviceRegistry = serviceRegistry;
		this.registration = registration;
	}

	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
		ServiceRegistry registry = serviceRegistry.getIfAvailable();
		Registration reg = registration.getIfAvailable();
		if (registry != null && reg != null) {
			try {
				logger.info("Pre-shutdown: deregistering {} from discovery before web server stops", reg.getServiceId());
				registry.deregister(reg);
			} catch (Exception ex) {
				logger.warn("Failed to deregister on pre-shutdown: {}", ex.getMessage());
			}
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}
}
