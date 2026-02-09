package com.marketcurrency.api.currency_rate_provider.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketcurrency.api.currency_rate_provider.rpc.dto.JsonRpcResponse;

@RestController
public class RpcController {
	private static final Logger logger = LoggerFactory.getLogger(RpcController.class);

	private final RpcDispatcher dispatcher;
	private final ObjectMapper objectMapper;

	public RpcController(RpcDispatcher dispatcher, ObjectMapper objectMapper) {
		this.dispatcher = dispatcher;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/rpc")
	public ResponseEntity<JsonRpcResponse> handle(@RequestBody String body) {
		long start = System.nanoTime();
		String method = "unknown";
		JsonRpcResponse response;
		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode methodNode = root.get("method");
			if (methodNode != null && methodNode.isTextual()) {
				method = methodNode.asText();
			}
			response = dispatcher.dispatch(root);
		} catch (Exception ex) {
			response = JsonRpcResponse.error(null, -32600, "Invalid Request");
		}
		long durationMs = (System.nanoTime() - start) / 1_000_000L;
		logger.info("rpc method={} durationMs={}", method, durationMs);
		return ResponseEntity.ok(response);
	}
}
