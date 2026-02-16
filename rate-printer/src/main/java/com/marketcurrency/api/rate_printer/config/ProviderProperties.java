package com.marketcurrency.api.rate_printer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "provider")
public class ProviderProperties {
	private String serviceId = "currency-rate-provider";
	private String rpcPath = "/rpc";
	private int connectTimeoutMs = 1000;
	private int readTimeoutMs = 1000;
	private int discoveryRefreshMs = 3000;

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getRpcPath() {
		return rpcPath;
	}

	public void setRpcPath(String rpcPath) {
		this.rpcPath = rpcPath;
	}

	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public int getReadTimeoutMs() {
		return readTimeoutMs;
	}

	public void setReadTimeoutMs(int readTimeoutMs) {
		this.readTimeoutMs = readTimeoutMs;
	}

	public int getDiscoveryRefreshMs() {
		return discoveryRefreshMs;
	}

	public void setDiscoveryRefreshMs(int discoveryRefreshMs) {
		this.discoveryRefreshMs = discoveryRefreshMs;
	}
}
