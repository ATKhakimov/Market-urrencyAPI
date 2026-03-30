# MarketCurrencyAPI

Два Spring Boot сервиса, общаются по JSON-RPC 2.0 / HTTP. Service discovery — ZooKeeper, балансировка — client-side round-robin.

- `currency-rate-provider` — отдаёт курс пары USDRUB по `POST /rpc`
- `rate-printer` — раз в 5с опрашивает provider и печатает курс

## Запуск

```bash
docker compose up -d zookeeper prometheus grafana
cd currency-rate-provider && ./gradlew bootRun
cd currency-rate-provider && ./gradlew bootRun --args='--server.port=8082'
cd rate-printer && ./gradlew bootRun
```

## Endpoints

- Provider: `http://localhost:8080/rpc` (и `:8082`)
- Client actuator: `http://localhost:8081/actuator`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin) — дашборды **JVM (Micrometer)** и **RPC Metrics**
- Pact Broker: `http://localhost:9292`

## Тесты и Pact

```bash
cd currency-rate-provider && ./gradlew test
cd rate-printer && ./gradlew test publishPactsToBroker
./run-pact-e2e.ps1
```

Под Java 25: `./run-tests-java25.ps1`
