package com.marketcurrency.api.currency_rate_provider.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.marketcurrency.api.currency_rate_provider.rpc.dto.JsonRpcResponse;

public interface RpcMethodHandler {
	String getMethod();

	JsonRpcResponse handle(JsonNode params, String id);
}
