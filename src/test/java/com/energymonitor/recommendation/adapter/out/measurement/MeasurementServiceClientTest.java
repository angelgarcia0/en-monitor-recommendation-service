package com.energymonitor.recommendation.adapter.out.measurement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.energymonitor.recommendation.adapter.out.measurement.MeasurementServiceClient.RemoteHourlyEnergy;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("Measurement service client")
class MeasurementServiceClientTest {

    private static final Instant FROM = Instant.parse("2026-10-24T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-10-24T23:59:59Z");

    private final RestClient.Builder builder = RestClient.builder()
            .baseUrl("http://measurement")
            .defaultHeader(MeasurementServiceClient.TOKEN_HEADER, "secret");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final MeasurementServiceClient client = new MeasurementServiceClient(builder.build());

    @Test
    @DisplayName("asks the hourly energy of every device in the range, sending the internal token")
    void hourlyEnergy() {
        server.expect(requestTo("http://measurement/internal/measurements/hourly-energy"
                        + "?deviceId=DEV0000001&deviceId=DEV0000002&from=2026-10-24T00:00:00Z&to=2026-10-24T23:59:59Z"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(MeasurementServiceClient.TOKEN_HEADER, "secret"))
                .andRespond(withSuccess("[{\"deviceId\":\"DEV0000001\",\"hourStart\":\"2026-10-24T10:00:00Z\","
                        + "\"averagePower\":200.0,\"energy\":0.2}]", MediaType.APPLICATION_JSON));

        assertThat(client.hourlyEnergy(List.of("DEV0000001", "DEV0000002"), FROM, TO)).containsExactly(
                new RemoteHourlyEnergy("DEV0000001", Instant.parse("2026-10-24T10:00:00Z"), 200.0, 0.2));
        server.verify();
    }

    @Test
    @DisplayName("no device asked for energy is no request")
    void noDevice() {
        assertThat(client.hourlyEnergy(List.of(), FROM, TO)).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("asks the devices reporting since an instant")
    void reportingDevices() {
        server.expect(requestTo("http://measurement/internal/measurements/reporting-devices?since=2026-10-24T00:00:00Z"))
                .andExpect(header(MeasurementServiceClient.TOKEN_HEADER, "secret"))
                .andRespond(withSuccess("[\"DEV0000001\"]", MediaType.APPLICATION_JSON));

        assertThat(client.reportingDevices(FROM)).containsExactly("DEV0000001");
    }

    @Test
    @DisplayName("a refused token, a server error or no answer is unavailable")
    void unavailable() {
        server.expect(requestTo("http://measurement/internal/measurements/reporting-devices?since=2026-10-24T00:00:00Z"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        server.expect(requestTo("http://measurement/internal/measurements/hourly-energy"
                        + "?deviceId=DEV0000001&from=2026-10-24T00:00:00Z&to=2026-10-24T23:59:59Z"))
                .andRespond(withServerError());
        server.expect(requestTo("http://measurement/internal/measurements/reporting-devices?since=2026-10-24T00:00:00Z"))
                .andRespond(withException(new IOException("refused")));

        assertThatThrownBy(() -> client.reportingDevices(FROM))
                .isInstanceOf(MeasurementServiceUnavailableException.class).hasMessageContaining("403");
        assertThatThrownBy(() -> client.hourlyEnergy(List.of("DEV0000001"), FROM, TO))
                .isInstanceOf(MeasurementServiceUnavailableException.class).hasMessageContaining("500");
        assertThatThrownBy(() -> client.reportingDevices(FROM))
                .isInstanceOf(MeasurementServiceUnavailableException.class);
    }
}
