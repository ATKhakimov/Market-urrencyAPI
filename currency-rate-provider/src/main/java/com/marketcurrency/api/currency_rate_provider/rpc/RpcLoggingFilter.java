package com.marketcurrency.api.currency_rate_provider.rpc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RpcLoggingFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(RpcLoggingFilter.class);
	private static final String RPC_PATH = "/rpc";

	private final MeterRegistry meterRegistry;

	public RpcLoggingFilter(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!RPC_PATH.equals(request.getRequestURI())) {
			chain.doFilter(request, response);
			return;
		}
		String client = headerOrDefault(request.getHeader("X-Client-Id"), "unknown");
		ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
		long start = System.nanoTime();
		try {
			chain.doFilter(req, resp);
		} finally {
			long durationNanos = System.nanoTime() - start;
			int status = resp.getStatus();
			String reqBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
			String respBody = new String(resp.getContentAsByteArray(), StandardCharsets.UTF_8);
			logger.info("server <- client={} status={} durationMs={} request={} response={}",
					client, status, durationNanos / 1_000_000L, reqBody, respBody);
			meterRegistry.counter("rpc.requests.total", "client", client).increment();
			if (status >= 500) {
				meterRegistry.counter("rpc.errors.total", "client", client, "status", String.valueOf(status)).increment();
			}
			Timer.builder("rpc.latency")
					.tag("client", client)
					.publishPercentiles(0.5, 0.95, 0.99)
					.publishPercentileHistogram()
					.register(meterRegistry)
					.record(Duration.ofNanos(durationNanos));
			resp.copyBodyToResponse();
		}
	}

	private static String headerOrDefault(String value, String fallback) {
		return (value == null || value.isBlank()) ? fallback : value;
	}
}
