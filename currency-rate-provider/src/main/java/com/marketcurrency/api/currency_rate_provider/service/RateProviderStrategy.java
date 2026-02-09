package com.marketcurrency.api.currency_rate_provider.service;

public interface RateProviderStrategy {
	double getRate(String pair);
}
