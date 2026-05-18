package com.marketcurrency.api.currency_rate_provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.marketcurrency.api.currency_rate_provider.GracefulShutdownIntegrationTest.SlowRateConfig;
import com.marketcurrency.api.currency_rate_provider.service.RateProviderStrategy;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = { CurrencyRateProviderApplication.class, SlowRateConfig.class }
)
@TestPropertySource(properties = {
		"spring.cloud.zookeeper.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"spring.cloud.service-registry.auto-registration.enabled=false",
		"spring.autoconfigure.exclude="
				+ "org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration,"
				+ "org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration,"
				+ "org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClientConfiguration,"
				+ "org.springframework.cloud.zookeeper.serviceregistry.ZookeeperAutoServiceRegistrationAutoConfiguration",
		"server.shutdown=graceful",
		"spring.lifecycle.timeout-per-shutdown-phase=10s"
})
class GracefulShutdownIntegrationTest {

	@LocalServerPort int port;
	@Autowired ApplicationContext context;

	@Test
	void inFlightRequestCompletesDuringGracefulShutdown() throws Exception {
		TestRestTemplate rest = new TestRestTemplate();
		AtomicReference<ResponseEntity<String>> ref = new AtomicReference<>();
		AtomicReference<Throwable> error = new AtomicReference<>();

		String body = "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"getRate\",\"params\":{\"pair\":\"USDRUB\"}}";
		CompletableFuture<Void> call = CompletableFuture.runAsync(() -> {
			try {
				ref.set(rest.postForEntity("http://localhost:" + port + "/rpc", body, String.class));
			} catch (Throwable t) {
				error.set(t);
			}
		});

		Thread.sleep(300);
		WebServer webServer = ((ServletWebServerApplicationContext) context).getWebServer();
		CountDownLatch shutdownDone = new CountDownLatch(1);
		new Thread(() -> webServer.shutDownGracefully(result -> shutdownDone.countDown())).start();

		call.get(15, TimeUnit.SECONDS);
		assertThat(shutdownDone.await(10, TimeUnit.SECONDS)).as("graceful shutdown must complete").isTrue();
		assertThat(error.get()).as("graceful shutdown should not abort in-flight request").isNull();
		assertThat(ref.get()).isNotNull();
		assertThat(ref.get().getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(ref.get().getBody()).contains("\"result\"");
	}

	@TestConfiguration
	static class SlowRateConfig {
		@Bean
		@Primary
		RateProviderStrategy slowStrategy() {
			return pair -> {
				try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
				return 100.0;
			};
		}
	}
}
