package com.marketcurrency.api.currency_rate_provider.rpc.dto;

public class JsonRpcResponse {
	private String jsonrpc;
	private String id;
	private Object result;
	private JsonRpcError error;

	public JsonRpcResponse() {
	}

	private JsonRpcResponse(String jsonrpc, String id, Object result, JsonRpcError error) {
		this.jsonrpc = jsonrpc;
		this.id = id;
		this.result = result;
		this.error = error;
	}

	public static JsonRpcResponse success(String id, Object result) {
		return new JsonRpcResponse("2.0", id, result, null);
	}

	public static JsonRpcResponse error(String id, int code, String message) {
		return new JsonRpcResponse("2.0", id, null, new JsonRpcError(code, message));
	}

	public String getJsonrpc() {
		return jsonrpc;
	}

	public void setJsonrpc(String jsonrpc) {
		this.jsonrpc = jsonrpc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public JsonRpcError getError() {
		return error;
	}

	public void setError(JsonRpcError error) {
		this.error = error;
	}
}
