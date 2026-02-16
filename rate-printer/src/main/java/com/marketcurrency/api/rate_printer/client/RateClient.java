package com.marketcurrency.api.rate_printer.client;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketcurrency.api.rate_printer.config.ProviderProperties;
import com.marketcurrency.api.rate_printer.dto.GetRateParams;
import com.marketcurrency.api.rate_printer.dto.GetRateResult;
import com.marketcurrency.api.rate_printer.dto.JsonRpcRequest;
import com.marketcurrency.api.rate_printer.dto.JsonRpcResponse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class RateClient {
	private static final Logger logger = LoggerFactory.getLogger(RateClient.class);

	private final ProviderProperties properties;
	private final ObjectMapper objectMapper;
	private final DiscoveryClient discoveryClient;
	private final LoadBalancerClient loadBalancerClient;
	private final RestTemplate restTemplate;
	private final AtomicLong idSequence = new AtomicLong(1);
	private final AtomicInteger availableInstancesGauge;
	private final Counter successCounter;
	private final Counter failureCounter;
	private final Timer callTimer;
	private volatile String lastKnownInstances = "";

	public RateClient(ProviderProperties properties,
			ObjectMapper objectMapper,
			DiscoveryClient discoveryClient,
			LoadBalancerClient loadBalancerClient,
			MeterRegistry meterRegistry) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.discoveryClient = discoveryClient;
		this.loadBalancerClient = loadBalancerClient;
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(properties.getConnectTimeoutMs());
		factory.setReadTimeout(properties.getReadTimeoutMs());
		this.restTemplate = new RestTemplate(factory);
		this.availableInstancesGauge = meterRegistry.gauge("provider.instances.available", new AtomicInteger(0));
		this.successCounter = Counter.builder("provider.calls.total")
				.tag("serviceId", properties.getServiceId())
				.tag("result", "success")
				.register(meterRegistry);
		this.failureCounter = Counter.builder("provider.calls.total")
				.tag("serviceId", properties.getServiceId())
				.tag("result", "failure")
				.register(meterRegistry);
		this.callTimer = Timer.builder("provider.call.latency")
				.tag("serviceId", properties.getServiceId())
				.register(meterRegistry);
	}

	@Scheduled(initialDelayString = "${provider.discovery-refresh-ms:3000}", fixedDelayString = "${provider.discovery-refresh-ms:3000}")
	public void refreshInstances() {
		List<ServiceInstance> instances = discoveryClient.getInstances(properties.getServiceId());
		availableInstancesGauge.set(instances.size());
		String snapshot = instances.stream()
				.sorted(Comparator.comparing(this::instanceKey))
				.map(this::instanceKey)
				.collect(Collectors.joining(","));

		if (!snapshot.equals(lastKnownInstances)) {
			lastKnownInstances = snapshot;
			logger.info("Provider instances updated: serviceId={}, count={}, instances=[{}]",
					properties.getServiceId(), instances.size(), snapshot);
		}
	}

	public GetRateResult getRate(String pair) {
		long startedAtNanos = System.nanoTime();
		String id = String.valueOf(idSequence.getAndIncrement());
		JsonRpcRequest request = new JsonRpcRequest(id, "getRate", new GetRateParams(pair));
		ServiceInstance selectedInstance = loadBalancerClient.choose(properties.getServiceId());
		if (selectedInstance == null) {
			failureCounter.increment();
			throw new RateClientException("No available provider instances for serviceId=" + properties.getServiceId());
		}

		String targetUrl = UriComponentsBuilder.fromUri(selectedInstance.getUri())
				.path(properties.getRpcPath())
				.toUriString();
		logger.info("Load balancer selected provider instance: requestId={}, serviceId={}, instanceId={}, host={}, port={}",
				id,
				properties.getServiceId(),
				instanceKey(selectedInstance),
				selectedInstance.getHost(),
				selectedInstance.getPort());

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, request, String.class);
			JsonRpcResponse rpcResponse = objectMapper.readValue(response.getBody(), JsonRpcResponse.class);
			if (rpcResponse.getError() != null) {
				failureCounter.increment();
				throw new RateClientException("RPC error: " + rpcResponse.getError().getMessage());
			}
			if (rpcResponse.getResult() == null) {
				failureCounter.increment();
				throw new RateClientException("Empty RPC result");
			}
			successCounter.increment();
			return rpcResponse.getResult();
		} catch (RestClientException ex) {
			failureCounter.increment();
			logger.warn("Provider call failed: requestId={}, serviceId={}, instanceId={}, reason={}",
					id,
					properties.getServiceId(),
					instanceKey(selectedInstance),
					ex.getMessage());
			throw new RateClientException("Request failed", ex);
		} catch (Exception ex) {
			failureCounter.increment();
			throw new RateClientException("Failed to parse RPC response", ex);
		} finally {
			callTimer.record(Duration.ofNanos(System.nanoTime() - startedAtNanos));
		}
	}

	private String instanceKey(ServiceInstance serviceInstance) {
		if (StringUtils.hasText(serviceInstance.getInstanceId())) {
			return serviceInstance.getInstanceId();
		}
		return serviceInstance.getHost() + ":" + serviceInstance.getPort();
	}
}
