# MarketCurrencyAPI

Два Spring Boot сервиса, общаются по JSON-RPC 2.0 / HTTP. Service discovery — ZooKeeper, балансировка — client-side round-robin.

- `currency-rate-provider` — отдаёт курс USDRUB по `POST /rpc`
- `rate-printer` — раз в 5с опрашивает provider и печатает курс

## Запуск (dev)

```bash
docker compose up -d zookeeper zipkin prometheus grafana
cd currency-rate-provider && ./gradlew bootRun
cd currency-rate-provider && ./gradlew bootRun --args='--server.port=8082'
cd rate-printer && ./gradlew bootRun
```

## Запуск (prod, всё в docker)

```bash
docker compose up -d --build
```

Профиль `prod` через `SPRING_PROFILES_ACTIVE=prod`.

## Endpoints

- Provider: `:8080/rpc` (dev) / `:18080`, `:18082` (prod)
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin) — JVM (Micrometer) и RPC Metrics
- Zipkin: `http://localhost:9411`
- Pact Broker: `http://localhost:9292`

## Тесты

```bash
cd currency-rate-provider && ./gradlew test
cd rate-printer && ./gradlew test publishPactsToBroker
./run-pact-e2e.ps1
```
