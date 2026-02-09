package com.marketcurrency.api.currency_rate_provider.service;

import org.springframework.stereotype.Service;

import com.marketcurrency.api.currency_rate_provider.rpc.dto.GetRateResult;

@Service
public class RateService {
	private final RateProviderStrategy rateProviderStrategy;

	public RateService(RateProviderStrategy rateProviderStrategy) {
		this.rateProviderStrategy = rateProviderStrategy;
	}

	public GetRateResult getRate(String pair) {
		double rate = rateProviderStrategy.getRate(pair);
		return new GetRateResult(pair, rate, System.currentTimeMillis());
	}
}
