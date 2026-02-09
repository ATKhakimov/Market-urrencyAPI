package com.marketcurrency.api.currency_rate_provider.rpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.marketcurrency.api.currency_rate_provider.rpc.dto.JsonRpcResponse;

@Component
public class RpcDispatcher {
	private final Map<String, RpcMethodHandler> handlers = new HashMap<>();

	public RpcDispatcher(List<RpcMethodHandler> handlerList) {
		for (RpcMethodHandler handler : handlerList) {
			handlers.put(handler.getMethod(), handler);
		}
	}

	public JsonRpcResponse dispatch(JsonNode root) {
		if (root == null || !root.isObject()) {
			return JsonRpcResponse.error(null, -32600, "Invalid Request");
		}
		String jsonrpc = textValue(root.get("jsonrpc"));
		if (!"2.0".equals(jsonrpc)) {
			return JsonRpcResponse.error(null, -32600, "Invalid Request");
		}
		String id = parseId(root.get("id"));
		if (id == null) {
			return JsonRpcResponse.error(null, -32600, "Invalid Request");
		}
		String method = textValue(root.get("method"));
		if (method == null || method.isBlank()) {
			return JsonRpcResponse.error(id, -32600, "Invalid Request");
		}
		RpcMethodHandler handler = handlers.get(method);
		if (handler == null) {
			return JsonRpcResponse.error(id, -32601, "Method not found");
		}
		try {
			return handler.handle(root.get("params"), id);
		} catch (InvalidParamsException ex) {
			return JsonRpcResponse.error(id, -32602, ex.getMessage());
		} catch (Exception ex) {
			return JsonRpcResponse.error(id, -32603, "Internal error");
		}
	}

	private String textValue(JsonNode node) {
		return node != null && node.isTextual() ? node.asText() : null;
	}

	private String parseId(JsonNode node) {
		if (node == null) {
			return null;
		}
		if (node.isTextual()) {
			return node.asText();
		}
		if (node.isNumber()) {
			return node.asText();
		}
		return null;
	}
}
