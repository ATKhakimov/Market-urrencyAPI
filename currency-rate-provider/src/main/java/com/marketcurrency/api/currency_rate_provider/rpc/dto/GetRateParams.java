package com.marketcurrency.api.currency_rate_provider.rpc.dto;

public class GetRateParams {
	private String pair;

	public GetRateParams() {
	}

	public GetRateParams(String pair) {
		this.pair = pair;
	}

	public String getPair() {
		return pair;
	}

	public void setPair(String pair) {
		this.pair = pair;
	}
}
