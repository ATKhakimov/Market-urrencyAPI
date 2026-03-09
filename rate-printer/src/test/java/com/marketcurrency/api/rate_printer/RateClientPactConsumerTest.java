package com.marketcurrency.api.rate_printer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import com.marketcurrency.api.rate_printer.client.RateClient;
import com.marketcurrency.api.rate_printer.config.ProviderProperties;
import com.marketcurrency.api.rate_printer.dto.GetRateResult;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "currency-rate-provider")
class RateClientPactConsumerTest {

	@Pact(consumer = "rate-printer")
	V4Pact getRatePact(PactBuilder builder) {
		DslPart requestBody = new PactDslJsonBody()
				.stringValue("jsonrpc", "2.0")
				.stringValue("id", "1")
				.stringValue("method", "getRate")
				.object("params")
				.stringValue("pair", "USDRUB")
				.closeObject();

		DslPart responseBody = new PactDslJsonBody()
				.stringValue("jsonrpc", "2.0")
				.stringValue("id", "1")
				.object("result")
				.stringValue("pair", "USDRUB")
				.decimalType("rate", 91.23)
				.integerType("timestampMs", 1739123456789L)
				.closeObject();

		return builder
				.given("USDRUB rate is available")
				.expectsToReceiveHttpInteraction("a JSON-RPC getRate request", interaction -> interaction
						.withRequest(request -> request
								.method("POST")
								.path("/rpc")
								.header("Content-Type", "application/json")
								.body(requestBody))
						.willRespondWith(response -> response
								.status(200)
								.header("Content-Type", "application/json")
								.body(responseBody)))
				.toPact();
	}

	@Test
	@PactTestFor(pactMethod = "getRatePact")
	void getRateReturnsDataFromProvider(MockServer mockServer) {
		URI providerUri = URI.create(mockServer.getUrl());

		ProviderProperties properties = new ProviderProperties();
		properties.setServiceId("currency-rate-provider");
		properties.setRpcPath("/rpc");

		DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
		when(discoveryClient.getInstances("currency-rate-provider")).thenReturn(Collections.emptyList());

		LoadBalancerClient loadBalancerClient = mock(LoadBalancerClient.class);
		when(loadBalancerClient.choose("currency-rate-provider")).thenReturn(new DefaultServiceInstance(
				"test-instance",
				"currency-rate-provider",
				providerUri.getHost(),
				providerUri.getPort(),
				"https".equalsIgnoreCase(providerUri.getScheme())));

		RateClient client = new RateClient(
				properties,
				new ObjectMapper(),
				discoveryClient,
				loadBalancerClient,
				new SimpleMeterRegistry());

		GetRateResult result = client.getRate("USDRUB");

		assertThat(result).isNotNull();
		assertThat(result.getPair()).isEqualTo("USDRUB");
		assertThat(result.getRate()).isPositive();
		assertThat(result.getTimestampMs()).isPositive();
	}
}
