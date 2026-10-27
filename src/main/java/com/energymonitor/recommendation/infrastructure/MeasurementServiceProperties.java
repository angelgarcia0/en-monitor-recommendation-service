package com.energymonitor.recommendation.infrastructure;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Where the measurement service answers and how long to wait for it. Its answers are not cached.
 *
 * @param url            base URL of the service, such as {@code http://measurement:8085}
 * @param connectTimeout how long to wait for the connection
 * @param readTimeout    how long to wait for the answer; the energy of a home over four weeks, for
 *                       the per-device rule, is the slowest one
 */
@ConfigurationProperties("measurement.service")
public record MeasurementServiceProperties(
        @DefaultValue("http://localhost:8085") URI url,
        @DefaultValue("PT1S") Duration connectTimeout,
        @DefaultValue("PT5S") Duration readTimeout) {
}
