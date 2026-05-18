package com.marketcurrency.api.currency_rate_provider.config;

import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Http2Config {

	@Bean
	public TomcatConnectorCustomizer h2cConnectorCustomizer() {
		return connector -> connector.addUpgradeProtocol(new Http2Protocol());
	}
}
