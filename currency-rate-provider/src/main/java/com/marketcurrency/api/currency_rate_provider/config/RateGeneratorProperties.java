package com.marketcurrency.api.currency_rate_provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate")
public class RateGeneratorProperties {
	private double baseRate = 90.0;
	private double dailyAmplitude = 1.2;
	private double dailyPhaseShift = 0.7;
	private double localAmplitude = 0.4;
	private double microAmplitude = 0.03;
	private long seed = 1L;
	private double clampPadding = 5.0;

	public double getBaseRate() {
		return baseRate;
	}

	public void setBaseRate(double baseRate) {
		this.baseRate = baseRate;
	}

	public double getDailyAmplitude() {
		return dailyAmplitude;
	}

	public void setDailyAmplitude(double dailyAmplitude) {
		this.dailyAmplitude = dailyAmplitude;
	}

	public double getDailyPhaseShift() {
		return dailyPhaseShift;
	}

	public void setDailyPhaseShift(double dailyPhaseShift) {
		this.dailyPhaseShift = dailyPhaseShift;
	}

	public double getLocalAmplitude() {
		return localAmplitude;
	}

	public void setLocalAmplitude(double localAmplitude) {
		this.localAmplitude = localAmplitude;
	}

	public double getMicroAmplitude() {
		return microAmplitude;
	}

	public void setMicroAmplitude(double microAmplitude) {
		this.microAmplitude = microAmplitude;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public double getClampPadding() {
		return clampPadding;
	}

	public void setClampPadding(double clampPadding) {
		this.clampPadding = clampPadding;
	}
}
