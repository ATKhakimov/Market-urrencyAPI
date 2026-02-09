package com.marketcurrency.api.currency_rate_provider.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.marketcurrency.api.currency_rate_provider.config.RateGeneratorProperties;

@Service
public class RandomJitterRateProvider implements RateProviderStrategy {
	private static final long DAY_MS = 24L * 60L * 60L * 1000L;
	private static final long MINUTE_MS = 60L * 1000L;

	private final RateGeneratorProperties properties;

	public RandomJitterRateProvider(RateGeneratorProperties properties) {
		this.properties = properties;
	}

	@Override
	public double getRate(String pair) {
		long now = System.currentTimeMillis();
		double daily = dailyComponent(now);
		double local = localComponent(now);
		double micro = microComponent(now);
		double rawRate = properties.getBaseRate() + daily + local + micro;
		double clamped = clamp(rawRate);
		return round4(clamped);
	}

	private double dailyComponent(long timeMs) {
		double dayPhase = (timeMs % DAY_MS) / (double) DAY_MS;
		double phase = 2.0 * Math.PI * dayPhase + properties.getDailyPhaseShift();
		return properties.getDailyAmplitude() * Math.sin(phase);
	}

	private double localComponent(long timeMs) {
		long minute = timeMs / MINUTE_MS;
		double u = (timeMs % MINUTE_MS) / (double) MINUTE_MS;
		double m0 = noise(minute);
		double m1 = noise(minute + 1);
		return properties.getLocalAmplitude() * lerp(m0, m1, u);
	}

	private double microComponent(long timeMs) {
		long sec = timeMs / 1000L;
		return properties.getMicroAmplitude() * noise(sec);
	}

	private double lerp(double a, double b, double u) {
		return a * (1.0 - u) + b * u;
	}

	private double clamp(double value) {
		double base = properties.getBaseRate();
		double padding = properties.getClampPadding();
		double min = base - padding;
		double max = base + padding;
		return Math.max(min, Math.min(max, value));
	}

	private double round4(double value) {
		return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
	}

	private double noise(long k) {
		long x = k ^ properties.getSeed();
		x ^= (x >>> 33);
		x *= 0xff51afd7ed558ccdL;
		x ^= (x >>> 33);
		x *= 0xc4ceb9fe1a85ec53L;
		x ^= (x >>> 33);
		double normalized = (double) (x >>> 11) / (double) (1L << 53);
		return normalized * 2.0 - 1.0;
	}
}
