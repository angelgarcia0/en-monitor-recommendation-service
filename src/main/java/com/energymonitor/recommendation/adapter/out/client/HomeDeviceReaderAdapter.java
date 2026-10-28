package com.energymonitor.recommendation.adapter.out.client;

import com.energymonitor.recommendation.adapter.out.device.DeviceServiceClient.RemoteDevice;
import com.energymonitor.recommendation.adapter.out.device.RemoteDevices;
import com.energymonitor.recommendation.adapter.out.home.HomeServiceClient.LimitPeriod;
import com.energymonitor.recommendation.adapter.out.home.RemoteHomes;
import com.energymonitor.recommendation.application.port.out.HomeReaderPort;
import com.energymonitor.recommendation.domain.model.ConsumptionLimit;
import com.energymonitor.recommendation.domain.model.MonitoredDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The devices of a home and their names, from the device service; the members and the limit of a
 * home, from the home service.
 */
@Component
public class HomeDeviceReaderAdapter implements HomeReaderPort {

    /** Appliance type seeded as {@code refrigerator} (device-006): it runs all night by design. */
    static final String REFRIGERATOR_TYPE_ID = "appl000001";

    private final RemoteDevices devices;
    private final RemoteHomes homes;

    public HomeDeviceReaderAdapter(RemoteDevices devices, RemoteHomes homes) {
        this.devices = devices;
        this.homes = homes;
    }

    @Override
    public Optional<String> homeOfDevice(String deviceId) {
        return devices.device(deviceId).map(RemoteDevice::homeId);
    }

    @Override
    public List<MonitoredDevice> devicesOf(String homeId) {
        return devices.devicesOfHome(homeId).stream()
                .map(d -> new MonitoredDevice(d.idDevice(), REFRIGERATOR_TYPE_ID.equals(d.applianceTypeId())))
                .toList();
    }

    @Override
    public Optional<ConsumptionLimit> limitOf(String homeId) {
        return homes.limits(homeId).map(l -> l.limitPeriod() == LimitPeriod.MONTHLY
                ? new ConsumptionLimit(ConsumptionLimit.Period.MONTHLY, l.monthlyLimit())
                : new ConsumptionLimit(ConsumptionLimit.Period.DAILY, l.dailyLimit()));
    }

    @Override
    public boolean isMember(String userId, String homeId) {
        return homes.isMember(userId, homeId);
    }

    @Override
    public Optional<String> deviceName(String deviceId) {
        return devices.device(deviceId).map(RemoteDevice::name);
    }
}
