package com.marketcurrency.api.currency_rate_provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marketcurrency.api.currency_rate_provider.service.RateProviderStrategy;

@SpringBootTest
class CurrencyRateProviderApplicationTests {

	@Autowired
	private RateProviderStrategy rateProviderStrategy;

	@Test
	void rateProviderReturnsValueInReasonableRange() {
		double base = 90.0;
		double rate = rateProviderStrategy.getRate("USDRUB");
		assertThat(rate).isBetween(base - 5.0, base + 5.0);
	}
}
