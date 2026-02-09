package com.marketcurrency.api.rate_printer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.marketcurrency.api.rate_printer.dto.JsonRpcRequest;

@SpringBootTest
class RatePrinterApplicationTests {

	@Test
	void jsonRpcRequestHasDefaultVersion() {
		JsonRpcRequest request = new JsonRpcRequest("1", "getRate", null);
		assertThat(request.getJsonrpc()).isEqualTo("2.0");
	}
}
