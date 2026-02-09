package com.marketcurrency.api.rate_printer.dto;

public class JsonRpcResponse {
	private String jsonrpc;
	private String id;
	private GetRateResult result;
	private JsonRpcError error;

	public JsonRpcResponse() {
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

	public GetRateResult getResult() {
		return result;
	}

	public void setResult(GetRateResult result) {
		this.result = result;
	}

	public JsonRpcError getError() {
		return error;
	}

	public void setError(JsonRpcError error) {
		this.error = error;
	}
}
