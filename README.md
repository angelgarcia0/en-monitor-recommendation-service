# en-monitor-recommendation-service

EnergyMonitor recommendation microservice: reviews the hourly consumption of every home that
reports, stores a recommendation when one of its rules matches (evening peak, night standby,
week-over-week rise, limit projection, device increase), lets the members of the home list, read
and delete them, and announces each new recommendation to the notification service. Spring Boot 4,
Java 21, hexagonal architecture.

It is being extracted from the `recommendation` module of `energy-monitor-backend`, the last one,
following the same steps as the security, device, measurement, home, notification and alert
services.

## Requirements

- Java 21 (the repository brings `./mvnw`, no Maven install needed).
- MySQL from the `infra` repository: the schema is shared with `energy-monitor-backend` and the
  security, device, measurement, home, notification and alert services.

## Run locally

```bash
cp .env.example .env
cp src/main/resources/application-local.yaml.example src/main/resources/application-local.yaml
# set the MYSQL_PASSWORD of infra/.env in both files
./mvnw spring-boot:run          # http://localhost:8089/actuator/health
```

On Windows PowerShell use `.\mvnw.cmd` instead of `./mvnw`.

## Tests

```bash
SPRING_DATASOURCE_PASSWORD=... ./mvnw test
```

The tests run under the `test` profile against the MySQL of `infra`.
