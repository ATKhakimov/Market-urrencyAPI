package com.marketcurrency.api.currency_rate_provider.rpc.handlers;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.marketcurrency.api.currency_rate_provider.rpc.InvalidParamsException;
import com.marketcurrency.api.currency_rate_provider.rpc.RpcMethodHandler;
import com.marketcurrency.api.currency_rate_provider.rpc.dto.GetRateResult;
import com.marketcurrency.api.currency_rate_provider.rpc.dto.JsonRpcResponse;
import com.marketcurrency.api.currency_rate_provider.service.RateService;

@Component
public class GetRateHandler implements RpcMethodHandler {
	private final RateService rateService;

	public GetRateHandler(RateService rateService) {
		this.rateService = rateService;
	}

	@Override
	public String getMethod() {
		return "getRate";
	}

	@Override
	public JsonRpcResponse handle(JsonNode params, String id) {
		String pair = extractPair(params);
		if (!"USDRUB".equals(pair)) {
			throw new InvalidParamsException("Only USDRUB supported");
		}
		GetRateResult result = rateService.getRate(pair);
		return JsonRpcResponse.success(id, result);
	}

	private String extractPair(JsonNode params) {
		if (params == null || !params.isObject()) {
			throw new InvalidParamsException("Missing params");
		}
		JsonNode pairNode = params.get("pair");
		if (pairNode == null || !pairNode.isTextual()) {
			throw new InvalidParamsException("Only USDRUB supported");
		}
		return pairNode.asText();
	}
}
