package com.marketcurrency.api.currency_rate_provider.rpc;

public class InvalidParamsException extends RuntimeException {
	public InvalidParamsException(String message) {
		super(message);
	}
}
