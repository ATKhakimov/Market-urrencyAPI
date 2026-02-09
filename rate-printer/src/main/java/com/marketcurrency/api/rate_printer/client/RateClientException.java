package com.marketcurrency.api.rate_printer.client;

public class RateClientException extends RuntimeException {
	public RateClientException(String message) {
		super(message);
	}

	public RateClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
