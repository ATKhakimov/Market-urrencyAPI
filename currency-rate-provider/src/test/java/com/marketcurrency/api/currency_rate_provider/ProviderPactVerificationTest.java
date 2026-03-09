package com.marketcurrency.api.currency_rate_provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
		properties = {
				"server.port=8080",
				"spring.main.web-application-type=servlet"
		})
@Provider("currency-rate-provider")
@PactBroker(url = "${PACT_BROKER_BASE_URL:http://localhost:9292}")
class ProviderPactVerificationTest {

	@Value("${local.server.port:8080}")
	private int port;

	@BeforeEach
	void before(PactVerificationContext context) {
		context.setTarget(new HttpTestTarget("localhost", port, "/"));
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void verifyPacts(PactVerificationContext context) {
		context.verifyInteraction();
	}

	@State("USDRUB rate is available")
	void usdrubRateIsAvailable() {
	}
}
