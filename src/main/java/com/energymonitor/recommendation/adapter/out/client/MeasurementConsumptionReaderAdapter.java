package com.energymonitor.recommendation.adapter.out.client;

import com.energymonitor.recommendation.adapter.out.measurement.MeasurementServiceClient;
import com.energymonitor.recommendation.application.port.out.ConsumptionReaderPort;
import com.energymonitor.recommendation.domain.model.HourSample;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Reads the hourly consumption and the devices that report from the measurement service.
 */
@Component
public class MeasurementConsumptionReaderAdapter implements ConsumptionReaderPort {

    private final MeasurementServiceClient measurements;

    public MeasurementConsumptionReaderAdapter(MeasurementServiceClient measurements) {
        this.measurements = measurements;
    }

    @Override
    public List<HourSample> hourly(Collection<String> deviceIds, Instant from, Instant to) {
        return measurements.hourlyEnergy(deviceIds, from, to).stream()
                .map(h -> new HourSample(h.deviceId(), h.hourStart(), h.averagePower(), h.energy()))
                .toList();
    }

    @Override
    public List<String> devicesReportingSince(Instant since) {
        return measurements.reportingDevices(since);
    }
}
