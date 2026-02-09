package com.marketcurrency.api.rate_printer.client;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketcurrency.api.rate_printer.config.ProviderProperties;
import com.marketcurrency.api.rate_printer.dto.GetRateParams;
import com.marketcurrency.api.rate_printer.dto.GetRateResult;
import com.marketcurrency.api.rate_printer.dto.JsonRpcRequest;
import com.marketcurrency.api.rate_printer.dto.JsonRpcResponse;

@Component
public class RateClient {
	private final ProviderProperties properties;
	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate;
	private final AtomicLong idSequence = new AtomicLong(1);

	public RateClient(ProviderProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(properties.getConnectTimeoutMs());
		factory.setReadTimeout(properties.getReadTimeoutMs());
		this.restTemplate = new RestTemplate(factory);
	}

	public GetRateResult getRate(String pair) {
		String id = String.valueOf(idSequence.getAndIncrement());
		JsonRpcRequest request = new JsonRpcRequest(id, "getRate", new GetRateParams(pair));
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(properties.getUrl(), request, String.class);
			JsonRpcResponse rpcResponse = objectMapper.readValue(response.getBody(), JsonRpcResponse.class);
			if (rpcResponse.getError() != null) {
				throw new RateClientException("RPC error: " + rpcResponse.getError().getMessage());
			}
			if (rpcResponse.getResult() == null) {
				throw new RateClientException("Empty RPC result");
			}
			return rpcResponse.getResult();
		} catch (RestClientException ex) {
			throw new RateClientException("Request failed", ex);
		} catch (Exception ex) {
			throw new RateClientException("Failed to parse RPC response", ex);
		}
	}
}
