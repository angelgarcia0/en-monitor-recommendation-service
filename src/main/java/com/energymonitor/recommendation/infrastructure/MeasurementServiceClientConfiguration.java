package com.energymonitor.recommendation.infrastructure;

import com.energymonitor.recommendation.adapter.out.measurement.MeasurementServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connects this service to the measurement service: the client of its internal API, with its
 * address, timeouts and the shared token.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MeasurementServiceProperties.class)
public class MeasurementServiceClientConfiguration {

    @Bean
    public MeasurementServiceClient measurementServiceClient(MeasurementServiceProperties properties,
                                                             @Value("${internal-api.token:}") String token) {
        return MeasurementServiceClient.create(properties.url().toString(), token == null ? "" : token,
                properties.connectTimeout(), properties.readTimeout());
    }
}
