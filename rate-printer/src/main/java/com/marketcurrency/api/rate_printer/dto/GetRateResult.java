package com.marketcurrency.api.rate_printer.dto;

public class GetRateResult {
	private String pair;
	private double rate;
	private long timestampMs;

	public GetRateResult() {
	}

	public GetRateResult(String pair, double rate, long timestampMs) {
		this.pair = pair;
		this.rate = rate;
		this.timestampMs = timestampMs;
	}

	public String getPair() {
		return pair;
	}

	public void setPair(String pair) {
		this.pair = pair;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public long getTimestampMs() {
		return timestampMs;
	}

	public void setTimestampMs(long timestampMs) {
		this.timestampMs = timestampMs;
	}
}
